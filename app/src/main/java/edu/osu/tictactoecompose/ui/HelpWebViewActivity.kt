package edu.osu.tictactoecompose.ui

import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme

/**
 * Code based on Ethan's WebView in Compose:
 *
 * (https://dev.to/ethand91/implementing-webview-with-jetpack-compose-7mi)
 *
 * StackOverflow
 *
 * https://stackoverflow.com/questions/78554742/my-pull-refresh-not-functioning-in-webview-compose,
 *
 * and Google's documentation:
 *
 * https://developer.android.com/develop/ui/compose/components/progress
 * https://developer.android.com/develop/ui/compose/quick-guides/content/manage-webview-state
 *
 * @author acc
 */
class HelpWebViewActivity: ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f),
                ) { innerPadding ->
                    HelpScreenWebView(innerPadding,
                        "https://en.wikipedia.org/wiki/Tic-tac-toe".toUri())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreenWebView(
    innerPadding: PaddingValues,
    url: Uri
) {
    var currentProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var loading by rememberSaveable { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth(1f)
        .safeDrawingPadding()
        .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (loading) {
            LinearProgressIndicator(
                progress = { currentProgress }
            )
        }
        AndroidView(
            factory = { context ->
                return@AndroidView WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.useWideViewPort = true
                    webChromeClient = object: WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            if (progress < 100) {
                                currentProgress = newProgress / 100f
                            } else {
                                loading = false
                            }
                        }
                    }

                }
            },
            update = {
                it.loadUrl(url.toString())
            }
        )
    }
}

@Preview(apiLevel = 35)
@Composable
fun HelpScreenWebViewPreview() {
    TicTacToeComposeTheme {
        HelpScreenWebView(
            innerPadding = PaddingValues(16.dp),
            url = "https://en.wikipedia.org/wiki/Tic-tac-toe".toUri()
        )
    }
}
