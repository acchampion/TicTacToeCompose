package edu.osu.tictactoecompose.ui

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.model.Intent
import edu.osu.tictactoecompose.model.PhotoAlbumViewState
import edu.osu.tictactoecompose.model.viewmodel.PhotoAlbumViewModel
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

/**
 * Adapted from Deniz Nassa's example:
 *
 * https://blog.eclypse.io/take-photo-or-pick-pictures-with-jetpack-compose-step-by-step-guide-a53cdced7c69
 */
class ImageActivity : ComponentActivity() {
    private lateinit var imageViewModel: PhotoAlbumViewModel

    private val classTag = javaClass.simpleName

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        imageViewModel = PhotoAlbumViewModel(coroutineContext = Dispatchers.Default)

        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f),
                ) { innerPadding ->
                    ImageScreen(
                        innerPadding = innerPadding,
                        imageViewModel
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ImageScreen(
        innerPadding: PaddingValues,
        viewModel: PhotoAlbumViewModel
    ) {
        val context = LocalContext.current
        val viewState: PhotoAlbumViewState by viewModel.photoAlbumViewState.collectAsState()

        val pickImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            viewModel.onReceive(Intent.OnFinishPickingImageWith(context, uris))
        }

        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { photoIsSaved ->
            if (photoIsSaved) {
                viewModel.onReceive(Intent.OnImageSavedWith(context))
            } else {
                viewModel.onReceive(Intent.OnImageSavingCanceled)
            }
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->
            if (permissionGranted) {
                viewModel.onReceive(Intent.OnPermissionGrantedWith(context))
            } else {
                viewModel.onReceive(Intent.OnPermissionDenied)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            Arrangement.spacedBy(24.dp)
        )
        {
            Text(
                text = "Photo Album",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text(text = stringResource(R.string.take_picture))
                }
                Button(
                    onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.select_picture))
                }
            }

            LaunchedEffect(key1 = viewState.tempPhotoUri) {
                viewState.tempPhotoUri?.let {
                    cameraLauncher.launch(it)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Selected Photos")
            LazyVerticalGrid(
                columns = GridCells.Adaptive(100.dp),
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, 600.dp)
            ) {
                itemsIndexed(viewState.selectedPhotos) { index, picture ->
                    Image(
                        modifier = Modifier.padding(8.dp),
                        bitmap = picture,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Images: Photo Album",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(innerPadding),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Button(
                onClick = {
                    Timber.tag("ImageScreen").d("You clicked button: 'Take Picture'")
                }
            ) {
                Text(text = stringResource(R.string.take_picture))
            }
            Button(
                onClick = {
                    Timber.tag("ImageScreen").d("You clicked button: 'Select Picture'")
                }
            ) {
                Text(text = stringResource(R.string.select_picture))
            }
        }
        Image(
            modifier = Modifier
                .fillMaxWidth(1f)
                .padding(innerPadding),
            painter = painterResource(R.drawable.scoreboard),
            contentDescription = "Photo of Tic-Tac-Toe scoreboard"
        )

    }
}


@Preview(apiLevel = 35)
@Composable
fun ImageScreenPreview() {
    TicTacToeComposeTheme {
        ImageScreen(
            innerPadding = PaddingValues(16.dp)
        )
    }
}
