package edu.osu.tictactoecompose.ui

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme

class HelpActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f),
                ) { innerPadding ->
                    HelpScreen(innerPadding)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    innerPadding: PaddingValues
) {

    Column(
        modifier = Modifier.fillMaxWidth(1f)
            .safeDrawingPadding()
            .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val activity = LocalActivity.current

        Text(
            text = stringResource(R.string.about_tictactoe),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.help_text),
            textAlign = TextAlign.Left
        )

        Text(
            text = stringResource(R.string.sources_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = stringResource(R.string.sources_text),
            textAlign = TextAlign.Left
        )

        OutlinedButton(
            content = {
                Text(stringResource(R.string.tictactoe_wikipedia))
            },
            onClick = {
                val intent = Intent(ACTION_VIEW, "https://en.wikipedia.org/wiki/Tic-tac-toe".toUri())
                activity?.startActivity(intent)
            }
        )

        OutlinedButton(
            content = {
                Text(stringResource(R.string.tictactoe_wikipedia_webview))
            },
            onClick = {
                val intent = Intent(activity, HelpWebViewActivity::class.java)
                activity?.startActivity(intent)
            }
        )
    }
}

@Preview(apiLevel = 35)
@Composable
fun HelpScreenPreview() {
    TicTacToeComposeTheme {
        HelpScreen(
            innerPadding = PaddingValues(16.dp)
        )
    }
}
