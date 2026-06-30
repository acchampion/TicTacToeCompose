package edu.osu.tictactoecompose.uistate

import edu.osu.tictactoecompose.model.GalleryItem

sealed interface PhotoGalleryUiState {
    data class Success(val photos: List<GalleryItem>) : PhotoGalleryUiState
    object Error : PhotoGalleryUiState
    object Loading : PhotoGalleryUiState
}