package edu.osu.tictactoecompose.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import timber.log.Timber
import kotlin.system.exitProcess

class GameOptionsActivity : ComponentActivity(), MenuProvider {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f),
                ) { innerPadding ->
                    GameOptionsScreen(innerPadding)
                }
            }
        }

        val menuHost: MenuHost = this
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.menu_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                return true
            }
        }
        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameOptionsScreen(
    innerPadding: PaddingValues
) {
    val activity = LocalActivity.current
    val openAlertDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Select an Option",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Button(
            content = {
                Text(text = "New Game")
            },
            onClick = {
                Timber.tag("GameOptionsActivity: Screen").d("Clicked New Game")

                val intent = Intent(activity, GameSessionActivity::class.java)
                activity?.startActivity(intent)
            }
        )
        OutlinedButton(
            content = {
                Text(text = "Media Player")
            },
            onClick = {
                val intent = Intent(activity, MediaSelectActivity::class.java)
                activity?.startActivity(intent)
            }
        )
        OutlinedButton(
            content = {
                Text(text = "Images: Photo Album")
            },
            onClick = {
                val intent = Intent(activity, ImageActivity::class.java)
                activity?.startActivity(intent)
            }
        )

        OutlinedButton(
            content = {
                Text(text = "Help")
            } ,
            onClick = {
                val intent = Intent(activity, HelpActivity::class.java)
                activity?.startActivity(intent)
            })
        OutlinedButton(
            content = {
                Text(text = "Maps: Location")
            },
            onClick = {
                val intent = Intent(activity, MapsActivity::class.java)
                activity?.startActivity(intent)
            }
        )
        OutlinedButton(
            content = {
                Text(text = "Settings")
            },
            onClick = {
                val intent = Intent(activity, SettingsActivity::class.java)
                activity?.startActivity(intent)
            }
        )
        OutlinedButton(
            content = {
                Text(text = "Flickr: Photo Gallery")
            },
            onClick = {
                val intent = Intent(activity, PhotoGalleryActivity::class.java)
                activity?.startActivity(intent)
            }
        )
        OutlinedButton(
            content = {
                Text(text = "Exit")
            },
            onClick = {
                Timber.tag("GameOptionsActivity: Screen").d("Clicked Exit")
                openAlertDialog.value = true
            }
        )

        if (openAlertDialog.value) {
            QuitAppAlertDialog(
                onDismissRequest = {
                    Timber.tag("Dialog").i("User declined")
                    openAlertDialog.value = false
                },
                onConfirmation = {
                    openAlertDialog.value = false
                    activity?.finish()
                    exitProcess(0)
                },
                dialogTitle = "Quit Tic-Tac-Toe?",
                dialogText = "Would you like to quit?",
                icon = Icons.Default.Info
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuitAppAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,

    ) {
    AlertDialog(
        icon = {
            Icon(
                icon,
                contentDescription = "Question Icon"
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Yes, quit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("No, keep playing")
            }
        }
    )
}

@Preview(apiLevel = 35)
@Composable
fun GameOptionsScreenPreview() {
    TicTacToeComposeTheme {
        GameOptionsScreen(PaddingValues(16.dp))
    }
}