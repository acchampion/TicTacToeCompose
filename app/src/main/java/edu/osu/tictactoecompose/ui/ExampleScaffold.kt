package edu.osu.tictactoecompose.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Customised Scaffold to be applied by all the activities. It adds the following UI elements to the
 * UI:
 *  * TopAppBar that shows the label for current activity.
 *  * Navigation icon that dismisses current activity, if the parent activity exists.
 *
 * Note: Edge-to-edge is enabled by default on Android 15 (API 35+).
 * The TopAppBar extends behind the status bar with internal padding to avoid content overlap.
 * See: https://developer.android.com/about/versions/15/behavior-changes-15#edge-to-edge
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ExampleScaffold(
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current
    val name = remember {
        activity?.title?.toString() ?: "Example"
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        topBar = {
            // Surface provides elevation and background for the entire top bar area including status bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                    TopAppBar(
                        title = { Text(text = name) },
                        navigationIcon = {
                            IconButton(onClick = { activity?.finish() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            }
        },
        bottomBar = bottomBar,
        content = content
    )
}