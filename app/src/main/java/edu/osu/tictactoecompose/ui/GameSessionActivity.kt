package edu.osu.tictactoecompose.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ExperimentalComposeViewContextApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.osu.tictactoecompose.GameGrid
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.SYMBOL
import edu.osu.tictactoecompose.Square
import edu.osu.tictactoecompose.model.viewmodel.GameViewModel
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class GameSessionActivity : ComponentActivity() {

    /* Assumption: the number of rows equals the number of columns */
    var numRows: Int = 3
    var humanPlaysFirst: Boolean = false

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModel.GameViewModelFactory(application)
    }

    private val classTag = javaClass.simpleName

    init {
        Timber.tag(classTag).d("Initializer running")
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f),
                ) { innerPadding ->
                    GameScreen(
                        innerPadding,
                        gameViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = this
        activity.actionBar?.apply {
            subtitle = resources.getString(R.string.game)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    innerPadding: PaddingValues,
    viewModel: GameViewModel = viewModel()
) {
    val gameUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showWinDialog = rememberSaveable { mutableStateOf(false) }
    val showDrawDialog = rememberSaveable { mutableStateOf(false) }
    val activity = LocalActivity.current

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Timber.tag("Activity-GameScreen").i("Started GameScreen composable")
        when (gameUiState.gameState) {
            STATE.Active -> {
                GameScreenGrid(innerPadding, viewModel, gameUiState)

                Text(
                    text = "${gameUiState.currentPlayerName} to play",
                    modifier = Modifier.padding(16.dp, 4.dp)
                )
                Text(
                    text =
                        "${gameUiState.firstPlayerName}: ${gameUiState.scorePlayerOne}....${gameUiState.secondPlayerName}: ${gameUiState.scorePlayerTwo}",
                    modifier = Modifier.padding(16.dp, 4.dp)
                )
            }

            STATE.Won -> {
                WinGameDialog(
                    onPlayAgain = {
                        viewModel.resetBoard()
                    },
                    viewModel,
                    gameUiState = gameUiState
                )
            }

            STATE.Draw -> {
                DrawGameDialog(
                    onPlayAgain = {
                        viewModel.resetBoard()
                    },
                    viewModel,
                    gameUiState = gameUiState
                )
            }

            STATE.Inactive -> {
                Timber.tag("GameScreen").i("Game is inactive")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeViewContextApi::class)
@Composable
fun GameScreenGrid(
    innerPadding: PaddingValues,
    gameViewModel: GameViewModel = viewModel(),
    gameUiState: GameUiState
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val grid: GameGrid by rememberSaveable { mutableStateOf(gameUiState.gameGrid) }
    val scope = rememberCoroutineScope()
    var gameState: STATE by rememberSaveable { mutableStateOf(gameUiState.gameState) }
    val showWinDialog = rememberSaveable { mutableStateOf(false) }
    val showDrawDialog = rememberSaveable { mutableStateOf(false) }
    val timeoutBase = rememberSaveable { mutableIntStateOf(500) }
    val timeoutSeed = rememberSaveable { mutableIntStateOf(1500) }
    val randomWaitGen = rememberSaveable { mutableStateOf(java.util.Random()) }
    val randomWait = rememberSaveable {
        mutableLongStateOf(
            Integer.sum(
                timeoutBase.intValue,
                randomWaitGen.value.nextInt(timeoutSeed.intValue)
            ).toLong()
        )
    }

    if (showWinDialog.value) {
        WinGameDialog(
            onPlayAgain = {
                //gameViewModel.resetBoard()
                gameViewModel.savePlayerScores()
                activity?.finish()
                val intent = Intent(activity, GameSessionActivity::class.java)
                activity?.startActivity(intent)
            },
            viewModel = gameViewModel,
            gameUiState = gameUiState
        )
    }

    if (showDrawDialog.value) {
        DrawGameDialog(
            onPlayAgain = {
                //gameViewModel.resetBoard()
                gameViewModel.savePlayerScores()
                activity?.finish()
                val intent = Intent(activity, GameSessionActivity::class.java)
                activity?.startActivity(intent)
            },
            viewModel = gameViewModel,
            gameUiState = gameUiState
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(grid.grid.size),
        modifier = Modifier
            .padding(16.dp)
            .aspectRatio(1.0F, true)
    ) {
        items(grid.toList()) { square ->
            var isBlank: Boolean by rememberSaveable { mutableStateOf(square.symbol == SYMBOL.Blank) }

            GameSquare(
                square,
                gameViewModel,
                onClicked = {
                    Timber.tag("Activity-GameGrid").d("Button (${square.x}, ${square.y}) clicked")
                    if (isBlank) {
                        Timber.tag("Activity-GameGrid")
                            .d("Current symbol: ${gameUiState.currentPlayerSymbol}")
                        //scope.launch {
                        val newSquare = gameViewModel.playerTakesATurn(square.x, square.y)
                        grid.grid[square.y][square.x].symbol = newSquare.symbol
                        Timber.tag("GameSquare")
                            .i("You put ${gameUiState.currentPlayerSymbol} at (${square.x}, ${square.y})")

                        if (grid.hasThreeInRow()) {
                            gameState = STATE.Won
                        } else if (gameUiState.playCount == 9) {
                            gameState = STATE.Draw
                        }

                        when (gameState) {
                            STATE.Active -> {
                                Timber.tag("Activity-GameGrid")
                                    .i("${gameViewModel.secondPlayerName} to play.")
                                scope.launch {
                                    delay(randomWait.longValue)
                                    val androidSquare = gameViewModel.androidTakesATurn()
                                    grid.grid[androidSquare.y][androidSquare.x].symbol =
                                        androidSquare.symbol
                                }
                            }

                            STATE.Won -> {
                                Timber.tag("Activity-GameGrid")
                                    .i("${gameViewModel.currentPlayerName} WON the game; ${gameViewModel.secondPlayerName} lost")
                                showWinDialog.value = true
                            }

                            STATE.Draw -> {
                                // There's a draw
                                Timber.tag("Activity-GameGrid")
                                    .i("DRAW between ${gameViewModel.currentPlayerName} and ${gameViewModel.secondPlayerName}")
                                showDrawDialog.value = true
                            }

                            STATE.Inactive -> {
                                Timber.tag("Activity-GameGrid").i("INACTIVE")
                            }

                        }
                        //}
                    } else {
                        Timber.tag("GameSquare")
                            .e("Square (${square.x}, ${square.y}) already clicked")
                        Toast.makeText(
                            context,
                            "There's already a ${square.symbol} in this square",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                gameUiState
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSquare(
    square: Square,
    gameViewModel: GameViewModel = viewModel(),
    onClicked: () -> Unit,
    gameUiState: GameUiState,
) {
    val context = LocalContext.current
    var isBlank: Boolean by rememberSaveable { mutableStateOf(square.symbol == SYMBOL.Blank) }

    Box(
        modifier = Modifier
            .defaultMinSize(48.dp, 48.dp)
            .aspectRatio(1.0F, true)
            .border(2.dp, MaterialTheme.colorScheme.primaryContainer)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            modifier = Modifier
                .defaultMinSize(72.dp, 72.dp)
                .fillMaxSize(1.0F)
                .aspectRatio(1.0F, true),
            onClick = onClicked,
            enabled = true
        ) {
            Icon(
                painter = when (square.symbol) {
                    SYMBOL.X -> painterResource(id = R.drawable.cross_48px)
                    SYMBOL.O -> painterResource(id = R.drawable.circle_48px)
                    SYMBOL.Blank -> painterResource(id = R.drawable.outline_square_48)
                },
                contentDescription = when (square.symbol) {
                    SYMBOL.X -> "X (cross) in position ${square.x}, ${square.y}"
                    SYMBOL.O -> "O (circle) in position ${square.x}, ${square.y}"
                    SYMBOL.Blank -> "Blank square in position ${square.x}, ${square.y}"
                },
                modifier = Modifier
                    .defaultMinSize(72.dp, 72.dp)
                    .aspectRatio(1.0F, true),
                tint = when (square.symbol) {
                    SYMBOL.X -> Color.Blue
                    SYMBOL.O -> Color.Red
                    SYMBOL.Blank -> Color.Transparent
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinGameDialog(
    onPlayAgain: () -> Unit,
    viewModel: GameViewModel,
    gameUiState: GameUiState
) {
    val activity = LocalActivity.current

    AlertDialog(
        onDismissRequest = { },
        title = { Text("${gameUiState.winningPlayerName} Won!") },
        text = { Text("Congratulations! Would you like to play again?") },
        dismissButton = {
            TextButton(
                onClick = {
                    Timber.tag("WinGameDialog").i("Clicked No button, finishing activity")
                    viewModel.resetPlayerScores()
                    activity?.finish()
                }
            ) {
                Text("No")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Timber.tag("WinGameDialog").i("Clicked Yes button, starting new game")
                    onPlayAgain()
                }
            ) {
                Text("Yes")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawGameDialog(
    onPlayAgain: () -> Unit,
    viewModel: GameViewModel,
    gameUiState: GameUiState
) {
    val activity = LocalActivity.current

    AlertDialog(
        onDismissRequest = { },
        title = { Text("Draw!") },
        text = { Text("Would you like to play again?") },
        dismissButton = {
            TextButton(
                onClick = {
                    Timber.tag("WinGameDialog").i("Clicked No button, finishing activity")
                    viewModel.resetPlayerScores()
                    activity?.finish()
                }
            ) {
                Text("No")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    Timber.tag("WinGameDialog").i("Clicked Yes button, starting new game")
                    onPlayAgain()
                }
            ) {
                Text("Yes")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    innerPadding: PaddingValues,
    grid: List<List<Square>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GameScreenGrid(innerPadding, grid)
        Text(
            text = "Player to play",
            modifier = Modifier.padding(16.dp, 4.dp)
        )
        Text(
            text = "Player: 0....Android: 0",
            modifier = Modifier.padding(16.dp, 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenGrid(
    innerPadding: PaddingValues,
    grid: List<List<Square>>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(grid[0].size),
        modifier = Modifier.padding(16.dp)
    ) {
        items(grid.flatten(), contentType = { (Square::class as Any).javaClass }) { square ->
            GameSquare(square)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSquare(
    square: Square
) {
    Box(
        modifier = Modifier
            .defaultMinSize(48.dp, 48.dp)
            .aspectRatio(1.0F, true)
            .border(2.dp, MaterialTheme.colorScheme.primaryContainer)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val context = LocalContext.current
        var isClicked: Boolean by rememberSaveable { mutableStateOf(false) }

        IconButton(
            modifier = Modifier
                .defaultMinSize(72.dp, 72.dp)
                .fillMaxSize(1.0F)
                .aspectRatio(1.0F, true),
            onClick = {
                isClicked = true
                Timber.d("Button (${square.x}, ${square.y}) clicked")
            },
            enabled = true
        ) {
            Icon(
                painter = when (square.symbol) {
                    SYMBOL.X -> painterResource(id = R.drawable.cross_48px)
                    SYMBOL.O -> painterResource(id = R.drawable.circle_48px)
                    SYMBOL.Blank -> painterResource(id = R.drawable.outline_square_48)
                },
                contentDescription = when (square.symbol) {
                    SYMBOL.X -> "X (cross) in position ${square.x}, ${square.y}"
                    SYMBOL.O -> "O (circle) in position ${square.x}, ${square.y}"
                    SYMBOL.Blank -> "Blank square in position ${square.x}, ${square.y}"
                },
                modifier = Modifier
                    .defaultMinSize(72.dp, 72.dp)
                    .aspectRatio(1.0F, true),
                tint = when (square.symbol) {
                    SYMBOL.X -> Color.Blue
                    SYMBOL.O -> Color.Red
                    SYMBOL.Blank -> Color.Transparent
                }
            )
        }
    }
}


@Preview(apiLevel = 35)
@Composable
fun GameScreenPreview() {
    TicTacToeComposeTheme {
        GameScreen(
            PaddingValues(16.dp),
            listOf<List<Square>>(
                listOf(
                    Square(0, 0, SYMBOL.O),
                    Square(0, 1, SYMBOL.Blank),
                    Square(0, 2, SYMBOL.Blank)
                ),
                listOf(
                    Square(1, 0, SYMBOL.Blank),
                    Square(1, 1, SYMBOL.X),
                    Square(1, 2, SYMBOL.Blank)
                ),
                listOf(
                    Square(2, 0, SYMBOL.O),
                    Square(2, 1, SYMBOL.Blank),
                    Square(2, 2, SYMBOL.X)
                )
            )
        )
    }
}