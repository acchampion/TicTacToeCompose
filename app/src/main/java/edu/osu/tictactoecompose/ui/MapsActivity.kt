package edu.osu.tictactoecompose.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.ViewportStatus
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import kotlinx.coroutines.launch

class MapsActivity : ComponentActivity() {

    lateinit var permissionsManager: PermissionsManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var permissionRequestCount by remember {
                mutableIntStateOf(1)
            }
            var showMap by remember {
                mutableStateOf(false)
            }
            var showRequestPermissionButton by remember {
                mutableStateOf(false)
            }
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(2.0)
                    center(Point.fromLngLat(-98.0, 39.5))
                    pitch(0.0)
                    bearing(0.0)
                }
            }

            TicTacToeComposeTheme {
                ExampleScaffold(
                    floatingActionButton = {
                        // Show locate button when viewport is in Idle state, e.g. camera is controlled by gestures.
                        if (mapViewportState.mapViewportStatus == ViewportStatus.Idle) {
                            FloatingActionButton(
                                onClick = {
                                    mapViewportState.transitionToFollowPuckState()
                                }
                            ) {
                                Image(
                                    painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                                    contentDescription = "Locate button"
                                )
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(snackbarHostState)
                    })
                {
                    RequestLocationPermission(
                        requestCount = permissionRequestCount,
                        onPermissionDenied = {
                            scope.launch {
                                snackbarHostState.showSnackbar("You need to accept location permissions.")
                            }
                            showRequestPermissionButton = true
                        },
                        onPermissionReady = {
                            showRequestPermissionButton = false
                            showMap = true
                        }
                    )
                    if (showMap) {
                        MapboxMap(
                            Modifier.fillMaxSize(),
                            mapViewportState = mapViewportState,
                        ) {
                            MapEffect(Unit) { mapView ->
                                mapView.location.updateSettings {
                                    locationPuck = createDefault2DPuck(withBearing = true)
                                    puckBearingEnabled = true
                                    puckBearing = PuckBearing.HEADING
                                    enabled = true
                                }
                                mapViewportState.transitionToFollowPuckState()
                            }
                        }
                    } else {
                        MapboxMap(
                            Modifier.fillMaxSize(),
                            mapViewportState = rememberMapViewportState {
                                setCameraOptions {
                                    zoom(2.0)
                                    center(Point.fromLngLat(-98.0, 39.5))
                                    pitch(0.0)
                                    bearing(0.0)
                                }
                            },
                            scaleBar = {
                                ScaleBar(Modifier.padding(top = 60.dp))
                            },
                            logo = {
                                Logo(Modifier.padding(bottom = 40.dp))
                            },
                            attribution = {
                                Attribution(Modifier.padding(bottom = 40.dp))
                            }
                        )
                    }
                    if (showRequestPermissionButton) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(modifier = Modifier.align(Alignment.Center)) {
                                Button(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    onClick = {
                                        permissionRequestCount += 1
                                    }
                                ) {
                                    Text("Request permission again ($permissionRequestCount)")
                                }
                                Button(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", packageName, null)
                                            )
                                        )
                                    }
                                ) {
                                    Text("Show App Settings page")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private companion object {
        const val ZOOM: Double = 0.0
        const val PITCH: Double = 0.0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapboxMapScreen(
    paddingValues: PaddingValues
) {
    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = rememberMapViewportState {
            setCameraOptions {
                zoom(2.0)
                center(Point.fromLngLat(-98.0, 39.5))
                pitch(0.0)
                bearing(0.0)
            }
        },
        scaleBar = {
            ScaleBar(Modifier.padding(top = 60.dp))
        },
        logo = {
            Logo(Modifier.padding(bottom = 40.dp))
        },
        attribution = {
            Attribution(Modifier.padding(bottom = 40.dp))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindLocationButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(Icons.Filled.MyLocation, "Find location button")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPreview() {
    MapboxMapScreen(PaddingValues(16.dp))
}