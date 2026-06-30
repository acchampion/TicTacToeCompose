package edu.osu.tictactoecompose.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wiley.fordummies.androidsdk.tictactoe.model.Settings
import edu.osu.tictactoecompose.GameGrid
import edu.osu.tictactoecompose.SYMBOL
import edu.osu.tictactoecompose.Square
import edu.osu.tictactoecompose.TicTacToeApplication
import edu.osu.tictactoecompose.model.SettingsDataStore
import edu.osu.tictactoecompose.uistate.GameUiState
import edu.osu.tictactoecompose.uistate.PLAYER
import edu.osu.tictactoecompose.uistate.STATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    var inTestMode: Boolean = false
    var humanPlaysFirst: Boolean = false
    var scorePlayerOne = 0
    var scorePlayerTwo = 0
    val androidPlayerName = Settings.Keys.OPT_NAME_COMPUTER
    var firstPlayerName: String = ""
    var secondPlayerName: String = ""
    var currentPlayerName: String = ""
    var winningPlayerName: String = ""

    var playCount: Int = 0

    private val classTag = javaClass.simpleName

    private val dataStore: SettingsDataStore = (application as TicTacToeApplication).dataStore

    var gameUiState: GameUiState = GameUiState()
    var _uiState: MutableStateFlow<GameUiState> = MutableStateFlow<GameUiState>(gameUiState)
    var uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    var gameState: STATE = STATE.Active
    var currentPlayerSymbol: SYMBOL = SYMBOL.X
    var currentPlayer: PLAYER = PLAYER.Player1
    var winningPlayer: PLAYER = PLAYER.Player1


    init {
        viewModelScope.launch {
            Timber.tag(classTag).d("Fetching first player, player names and scores from DataStore")
            Timber.tag(classTag).i("Init start")

            withContext(Dispatchers.IO) {
                inTestMode = dataStore.getBoolean("is_test_mode", false)
                humanPlaysFirst = dataStore.getBoolean(
                    Settings.Keys.OPT_PLAY_FIRST,
                    Settings.Keys.OPT_PLAY_FIRST_DEF
                )

                currentPlayerSymbol = if (humanPlaysFirst) {
                    SYMBOL.X
                } else {
                    SYMBOL.O
                }

                scorePlayerOne = dataStore.getInt(Settings.Keys.SCOREPLAYERONEKEY, 0)
                scorePlayerTwo = dataStore.getInt(Settings.Keys.SCOREPLAYERTWOKEY, 0)
                Timber.tag(classTag)
                    .i("Loaded player scores: player 1's score is $scorePlayerOne; player 2's score is $scorePlayerTwo")

                if (humanPlaysFirst) {
                    firstPlayerName =
                        dataStore.getString(Settings.Keys.OPT_NAME, Settings.Keys.OPT_NAME_DEFAULT)
                    secondPlayerName = androidPlayerName
                    currentPlayerName = firstPlayerName
                    currentPlayerSymbol = SYMBOL.X
                } else {
                    firstPlayerName = androidPlayerName
                    secondPlayerName =
                        dataStore.getString(Settings.Keys.OPT_NAME, Settings.Keys.OPT_NAME_DEFAULT)
                    currentPlayerName = secondPlayerName
                    currentPlayerSymbol = SYMBOL.O
                }
            }

            gameUiState = GameUiState(
                firstPlayerName = firstPlayerName,
                secondPlayerName = secondPlayerName,
                currentPlayerName = currentPlayerName,
                currentPlayerSymbol = currentPlayerSymbol,
                scorePlayerOne = scorePlayerOne,
                scorePlayerTwo = scorePlayerTwo,
                playCount = 0,
            )
            _uiState.update { currentUiState ->
                gameUiState.copy(
                    firstPlayerName = firstPlayerName,
                    secondPlayerName = secondPlayerName,
                    currentPlayerName = currentPlayerName,
                    currentPlayerSymbol = currentPlayerSymbol,
                    scorePlayerOne = scorePlayerOne,
                    scorePlayerTwo = scorePlayerTwo,
                    playCount = 0,
                )
            }
            uiState = _uiState.asStateFlow()

            Timber.tag(classTag)
                .i(
                    "Init done. First player: ${firstPlayerName}; Second player: ${secondPlayerName}; " +
                            "Current player: ${currentPlayerName}; Player 1 score: ${scorePlayerOne}; Player 2 score: ${scorePlayerTwo}"
                )

            if (!humanPlaysFirst && playCount == 0) {
                val newGameGrid: GameGrid = gameUiState.gameGrid.deepCopy()
                gameState = gameUiState.gameState
                val symbol: SYMBOL = currentPlayerSymbol
                val square: Square = androidTakesATurn()

                if (newGameGrid.grid[square.y][square.x].symbol == SYMBOL.Blank) {
                    newGameGrid.grid[square.y][square.x].symbol = currentPlayerSymbol
                    Timber.tag(classTag)
                        .i("First move: place symbol $currentPlayerSymbol at position (${square.x}, ${square.y})")

                    val nameTriple: Triple<String, String, String> = Triple(
                        gameUiState.firstPlayerName,
                        gameUiState.secondPlayerName,
                        gameUiState.currentPlayerName
                    )
                    val newNameTriple = swapPlayerNamesSetCurrentPlayer(nameTriple)
                    playCount += 1

                    currentPlayerSymbol = if (currentPlayerSymbol == SYMBOL.X) {
                        SYMBOL.O
                    } else {
                        SYMBOL.X
                    }
                    currentPlayer = if (gameUiState.currentPlayer == PLAYER.Player1) {
                        PLAYER.Player2
                    } else {
                        PLAYER.Player1
                    }

                    gameUiState = GameUiState(
                        gameGrid = newGameGrid,
                        firstPlayerName = newNameTriple.first,
                        secondPlayerName = newNameTriple.second,
                        currentPlayer = currentPlayer,
                        winningPlayer = currentPlayer,
                        currentPlayerName = newNameTriple.third,
                        winningPlayerName = "",
                        currentPlayerSymbol = currentPlayerSymbol,
                        scorePlayerOne = scorePlayerOne,
                        scorePlayerTwo = scorePlayerTwo,
                        gameState = STATE.Active,
                        playCount = playCount
                    )

                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            gameGrid = newGameGrid,
                            firstPlayerName = newNameTriple.first,
                            secondPlayerName = newNameTriple.second,
                            currentPlayer = currentPlayer,
                            winningPlayer = currentPlayer,
                            currentPlayerName = newNameTriple.third,
                            winningPlayerName = "",
                            currentPlayerSymbol = currentPlayerSymbol,
                            scorePlayerOne = scorePlayerOne,
                            scorePlayerTwo = scorePlayerTwo,
                            gameState = STATE.Active,
                            playCount = playCount
                        )
                    }
                    uiState = _uiState.asStateFlow()
                }
            }
        }
    }


    fun swapPlayerNamesSetCurrentPlayer(triple: Triple<String, String, String>): Triple<String, String, String> {
        var firstName = triple.first
        var secondName = triple.second
        var currentName = triple.third

        if (firstName == androidPlayerName) {
            firstName = secondName
            secondName = androidPlayerName
            currentName = firstName
        } else {
            currentName = secondName
            secondName = firstName
            firstName = androidPlayerName
        }

        return Triple(firstName, secondName, currentName)
    }

    fun swapPlayerNames() {
        if (firstPlayerName == androidPlayerName) {
            /* The first player is Android. Flip the first and second player names */
            firstPlayerName = secondPlayerName
            secondPlayerName = androidPlayerName
            currentPlayerName = firstPlayerName
        } else {
            /* The second player is Android. Flip the first and second player names */
            secondPlayerName = firstPlayerName
            firstPlayerName = androidPlayerName
            currentPlayerName = secondPlayerName
        }
    }

    fun loadPlayerScores() {
        Timber.tag(classTag)
            .d("loadPlayerScores(): Player 1 score: $scorePlayerOne; player 2 score: $scorePlayerTwo")

        CoroutineScope(Dispatchers.IO).launch {
            scorePlayerOne = dataStore.getInt(Settings.Keys.SCOREPLAYERONEKEY, 0)
            Timber.tag(classTag)
                .i("Coroutine: Read Player 1 score $scorePlayerOne successfully from DataStore")
            scorePlayerTwo = dataStore.getInt(Settings.Keys.SCOREPLAYERTWOKEY, 0)
            Timber.tag(classTag)
                .i("Coroutine: Read Player 2 score $scorePlayerTwo successfully from DataStore")
        }
    }

    private fun accumulateScores(winningPlayerName: String) {
        if (winningPlayerName == firstPlayerName)
            scorePlayerOne++
        else
            scorePlayerTwo++
    }

    fun savePlayerScores() {
        Timber.tag(classTag)
            .d("savePlayerScores(): Player 1 score: $scorePlayerOne; player 2 score: $scorePlayerTwo")

        CoroutineScope(Dispatchers.IO).launch {
            dataStore.putInt(Settings.Keys.SCOREPLAYERONEKEY, scorePlayerOne)
            Timber.tag(classTag)
                .i("Coroutine: wrote Player 1 score $scorePlayerOne successfully to DataStore")
            dataStore.putInt(Settings.Keys.SCOREPLAYERTWOKEY, scorePlayerTwo)
            Timber.tag(classTag)
                .i("Coroutine: Wrote Player 2 score $scorePlayerTwo successfully to DataStore")
        }
    }

    fun playerTakesATurn(x: Int, y: Int): Square {
        val newGameGrid: GameGrid = gameUiState.gameGrid.deepCopy()
        gameState = gameUiState.gameState
        val symbol: SYMBOL = currentPlayerSymbol

        if (newGameGrid.grid[y][x].symbol == SYMBOL.Blank) {
            newGameGrid.grid[y][x].symbol = currentPlayerSymbol
            Timber.tag(classTag).i("Place symbol $currentPlayerSymbol at position ($x, $y)")

            val nameTriple: Triple<String, String, String> = Triple(
                gameUiState.firstPlayerName,
                gameUiState.secondPlayerName,
                gameUiState.currentPlayerName
            )
            val newNameTriple = swapPlayerNamesSetCurrentPlayer(nameTriple)
            playCount += 1

            if (newGameGrid.hasThreeInRow()) {
                gameState = STATE.Won
                winningPlayer = currentPlayer
                winningPlayerName = currentPlayerName
            } else if (playCount == 9) {
                gameState = STATE.Draw
            }

            when (gameState) {
                STATE.Won -> {
                    gameUiState = GameUiState(
                        gameGrid = newGameGrid,
                        firstPlayerName = gameUiState.firstPlayerName,
                        secondPlayerName = gameUiState.secondPlayerName,
                        currentPlayer = currentPlayer,
                        winningPlayer = winningPlayer,
                        winningPlayerName = currentPlayerName,
                        currentPlayerName = currentPlayerName,
                        currentPlayerSymbol = currentPlayerSymbol,
                        scorePlayerOne = scorePlayerOne,
                        scorePlayerTwo = scorePlayerTwo,
                        gameState = STATE.Won,
                        playCount = playCount
                    )

                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            gameGrid = newGameGrid,
                            firstPlayerName = gameUiState.firstPlayerName,
                            secondPlayerName = gameUiState.secondPlayerName,
                            currentPlayer = currentPlayer,
                            winningPlayer = winningPlayer,
                            currentPlayerName = currentPlayerName,
                            winningPlayerName = currentPlayerName,
                            currentPlayerSymbol = currentPlayerSymbol,
                            scorePlayerOne = scorePlayerOne,
                            scorePlayerTwo = scorePlayerTwo,
                            gameState = STATE.Won,
                            playCount = playCount
                        )
                    }
                    uiState = _uiState.asStateFlow()
                    proceedToFinish()
                }

                STATE.Draw -> {
                    gameUiState = GameUiState(
                        gameGrid = newGameGrid,
                        firstPlayerName = gameUiState.firstPlayerName,
                        secondPlayerName = gameUiState.secondPlayerName,
                        currentPlayer = currentPlayer,
                        winningPlayer = winningPlayer,
                        winningPlayerName = "",
                        currentPlayerName = currentPlayerName,
                        currentPlayerSymbol = currentPlayerSymbol,
                        scorePlayerOne = scorePlayerOne,
                        scorePlayerTwo = scorePlayerTwo,
                        gameState = STATE.Draw,
                        playCount = playCount + 1
                    )

                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            gameGrid = newGameGrid,
                            firstPlayerName = gameUiState.firstPlayerName,
                            secondPlayerName = gameUiState.secondPlayerName,
                            currentPlayer = currentPlayer,
                            winningPlayer = winningPlayer,
                            winningPlayerName = "",
                            currentPlayerName = currentPlayerName,
                            currentPlayerSymbol = currentPlayerSymbol,
                            scorePlayerOne = scorePlayerOne,
                            scorePlayerTwo = scorePlayerTwo,
                            gameState = STATE.Draw,
                            playCount = playCount
                        )
                    }

                    savePlayerScores()
                }

                STATE.Inactive -> {
                    Timber.tag(classTag).e("Error: Game is inactive")
                }

                STATE.Active -> {
                    currentPlayerSymbol = if (currentPlayerSymbol == SYMBOL.X) {
                        SYMBOL.O
                    } else {
                        SYMBOL.X
                    }
                    currentPlayer = if (gameUiState.currentPlayer == PLAYER.Player1) {
                        PLAYER.Player2
                    } else {
                        PLAYER.Player1
                    }

                    gameUiState = GameUiState(
                        gameGrid = newGameGrid,
                        firstPlayerName = newNameTriple.first,
                        secondPlayerName = newNameTriple.second,
                        currentPlayer = currentPlayer,
                        winningPlayer = currentPlayer,
                        currentPlayerName = newNameTriple.third,
                        winningPlayerName = "",
                        currentPlayerSymbol = currentPlayerSymbol,
                        scorePlayerOne = scorePlayerOne,
                        scorePlayerTwo = scorePlayerTwo,
                        gameState = STATE.Active,
                        playCount = playCount
                    )

                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            gameGrid = newGameGrid,
                            firstPlayerName = newNameTriple.first,
                            secondPlayerName = newNameTriple.second,
                            currentPlayer = currentPlayer,
                            winningPlayer = currentPlayer,
                            currentPlayerName = newNameTriple.third,
                            winningPlayerName = "",
                            currentPlayerSymbol = currentPlayerSymbol,
                            scorePlayerOne = scorePlayerOne,
                            scorePlayerTwo = scorePlayerTwo,
                            gameState = STATE.Active,
                            playCount = playCount
                        )
                    }
                    uiState = _uiState.asStateFlow()
                }
            }
        } else {
            Timber.tag(classTag)
                .e("Error: Square ($x, $y) already has symbol ${newGameGrid.grid[x][y].symbol}")
        }

        return Square(x, y, symbol)
    }

    fun androidTakesATurn(): Square {
        val newGameGrid: GameGrid = gameUiState.gameGrid.deepCopy()
        val emptySquares = newGameGrid.emptySquares
        val r = Random()
        var randomIndex = r.nextInt(emptySquares.size)
        var emptySquare = emptySquares[randomIndex]

        while (newGameGrid.grid[emptySquare.y][emptySquare.x].symbol != SYMBOL.Blank) {
            Timber.tag(classTag)
                .e("Error: Square (${emptySquare.x}, ${emptySquare.y}) has symbol " +
                        "${newGameGrid.grid[emptySquare.y][emptySquare.x].symbol}")
            randomIndex = r.nextInt(emptySquares.size)
            emptySquare = emptySquares[randomIndex]
        }

        val square: Square = playerTakesATurn(emptySquare.x, emptySquare.y)
        return square
    }

    fun proceedToFinish() {
        winningPlayerName = currentPlayerName
        accumulateScores(currentPlayerName)
        savePlayerScores()
    }

    fun resetPlayerScores() {
        Timber.tag(classTag).d("Player 1 score: $scorePlayerOne; player 2 score: $scorePlayerTwo")
        Timber.tag(classTag).d("Resetting both scores to zero")

        CoroutineScope(Dispatchers.IO).launch {
            dataStore.putInt(Settings.Keys.SCOREPLAYERONEKEY, 0)
            Timber.tag(classTag)
                .i("Coroutine: Reset Player 1 score to 0; saved to DataStore")
            dataStore.putInt(Settings.Keys.SCOREPLAYERTWOKEY, 0)
            Timber.tag(classTag)
                .i("Coroutine: Reset Player 2 score to 0; saved to DataStore")
        }
    }

    fun resetBoard() {
        viewModelScope.launch {
            Timber.tag(classTag).i("resetBoard(): loading player scores")
            scorePlayerOne = dataStore.getInt(Settings.Keys.SCOREPLAYERONEKEY, 0)
            scorePlayerTwo = dataStore.getInt(Settings.Keys.SCOREPLAYERTWOKEY, 0)
            Timber.tag(classTag)
                .i("Loaded player scores: player 1's score is $scorePlayerOne; player 2's score is $scorePlayerTwo")

            if (humanPlaysFirst) {
                firstPlayerName =
                    dataStore.getString(Settings.Keys.OPT_NAME, Settings.Keys.OPT_NAME_DEFAULT)
                secondPlayerName = androidPlayerName
                currentPlayerName = firstPlayerName
                currentPlayerSymbol = SYMBOL.X
            } else {
                firstPlayerName = androidPlayerName
                secondPlayerName =
                    dataStore.getString(Settings.Keys.OPT_NAME, Settings.Keys.OPT_NAME_DEFAULT)
                currentPlayerName = secondPlayerName
                currentPlayerSymbol = SYMBOL.O
            }

            playCount = 0

            Timber.tag(classTag).i("resetBoard(): recreating GameUiState, updating Flow")
            gameUiState = GameUiState(
                firstPlayerName = firstPlayerName,
                secondPlayerName = secondPlayerName,
                currentPlayerName = currentPlayerName,
                currentPlayerSymbol = currentPlayerSymbol,
                scorePlayerOne = scorePlayerOne,
                scorePlayerTwo = scorePlayerTwo,
                playCount = 0
            )
            _uiState.update { currentUiState ->
                gameUiState.copy(
                    firstPlayerName = firstPlayerName,
                    secondPlayerName = secondPlayerName,
                    currentPlayerName = currentPlayerName,
                    currentPlayerSymbol = currentPlayerSymbol,
                    scorePlayerOne = scorePlayerOne,
                    scorePlayerTwo = scorePlayerTwo,
                    playCount = 0
                )
            }
            uiState = _uiState.asStateFlow()
            Timber.tag(classTag).i("resetBoard(): finished")

            if (!humanPlaysFirst && playCount == 0) {
                val newGameGrid: GameGrid = gameUiState.gameGrid.deepCopy()
                gameState = gameUiState.gameState
                val symbol: SYMBOL = currentPlayerSymbol
                val square: Square = androidTakesATurn()

                if (newGameGrid.grid[square.y][square.x].symbol == SYMBOL.Blank) {
                    newGameGrid.grid[square.y][square.x].symbol = currentPlayerSymbol
                    Timber.tag(classTag)
                        .i("First move: place symbol $currentPlayerSymbol at position (${square.x}, ${square.y})")

                    val nameTriple: Triple<String, String, String> = Triple(
                        gameUiState.firstPlayerName,
                        gameUiState.secondPlayerName,
                        gameUiState.currentPlayerName
                    )
                    val newNameTriple = swapPlayerNamesSetCurrentPlayer(nameTriple)
                    playCount += 1

                    currentPlayerSymbol = if (currentPlayerSymbol == SYMBOL.X) {
                        SYMBOL.O
                    } else {
                        SYMBOL.X
                    }
                    currentPlayer = if (gameUiState.currentPlayer == PLAYER.Player1) {
                        PLAYER.Player2
                    } else {
                        PLAYER.Player1
                    }

                    gameUiState = GameUiState(
                        gameGrid = newGameGrid,
                        firstPlayerName = newNameTriple.first,
                        secondPlayerName = newNameTriple.second,
                        currentPlayer = currentPlayer,
                        winningPlayer = currentPlayer,
                        currentPlayerName = newNameTriple.third,
                        winningPlayerName = "",
                        currentPlayerSymbol = currentPlayerSymbol,
                        scorePlayerOne = scorePlayerOne,
                        scorePlayerTwo = scorePlayerTwo,
                        gameState = STATE.Active,
                        playCount = playCount
                    )

                    _uiState.update { currentUiState ->
                        currentUiState.copy(
                            gameGrid = newGameGrid,
                            firstPlayerName = newNameTriple.first,
                            secondPlayerName = newNameTriple.second,
                            currentPlayer = currentPlayer,
                            winningPlayer = currentPlayer,
                            currentPlayerName = newNameTriple.third,
                            winningPlayerName = "",
                            currentPlayerSymbol = currentPlayerSymbol,
                            scorePlayerOne = scorePlayerOne,
                            scorePlayerTwo = scorePlayerTwo,
                            gameState = STATE.Active,
                            playCount = playCount
                        )
                    }
                    uiState = _uiState.asStateFlow()
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class GameViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}