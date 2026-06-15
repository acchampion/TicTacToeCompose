package edu.osu.tictactoecompose.model

sealed interface PhotoGalleryUiState {
    data class Success(val photos: List<GalleryItem>) : PhotoGalleryUiState
    object Error : PhotoGalleryUiState
    object Loading : PhotoGalleryUiState
}