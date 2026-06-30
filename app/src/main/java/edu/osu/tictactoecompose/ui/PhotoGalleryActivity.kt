package edu.osu.tictactoecompose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.model.GalleryItem
import edu.osu.tictactoecompose.model.viewmodel.PhotoGalleryViewModel
import edu.osu.tictactoecompose.ui.theme.TicTacToeComposeTheme
import edu.osu.tictactoecompose.uistate.PhotoGalleryUiState

class PhotoGalleryActivity : ComponentActivity() {
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels {
        PhotoGalleryViewModel.PhotoGalleryViewModelFactory(application)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(1f)
                )
                { innerPadding ->
                    PhotoGalleryScreen(
                        innerPadding,
                        photoGalleryViewModel.photoGalleryUiState,
                        modifier = Modifier.fillMaxWidth(1f),
                        photoGalleryViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGalleryScreen(
    innerPadding: PaddingValues,
    photoGalleryUiState: PhotoGalleryUiState,
    modifier: Modifier = Modifier,
    photoViewModel: PhotoGalleryViewModel = viewModel()
) {
    when (photoGalleryUiState) {
        is PhotoGalleryUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize(1f))
        is PhotoGalleryUiState.Success -> ResultScreen(
            photoGalleryUiState.photos,
            modifier = Modifier.fillMaxWidth()
        )

        is PhotoGalleryUiState.Error -> ErrorScreen(modifier = Modifier.fillMaxSize())
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier.size(250.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    photos: List<GalleryItem>,
    modifier: Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth(1f),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Photo Gallery",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp)
        ) {
            items(
                count = photos.size,
                key = { index -> photos[index].id.toLong() }) { photo ->
                PhotoCard(photos[photo])
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCard(
    photo: GalleryItem,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(context = LocalContext.current)
            .data(photo.photoUri)
            .crossfade(true)
            .build(),
        contentDescription = photo.title,
        error = painterResource(R.drawable.ic_broken_image),
        placeholder = painterResource(R.drawable.ic_placeholder),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error), contentDescription = ""
        )
        Text(text = stringResource(R.string.loading_failed), modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    TicTacToeComposeTheme() {
        LoadingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    TicTacToeComposeTheme() {
        ErrorScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PhotosGridScreenPreview() {
    TicTacToeComposeTheme() {
        val mockData = List(10) { GalleryItem(id = "$it", "") }
        ResultScreen(
            mockData,
            modifier = Modifier.padding(8.dp)
        )
    }
}