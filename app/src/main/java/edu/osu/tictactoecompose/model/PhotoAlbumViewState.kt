package edu.osu.tictactoecompose.model

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Adapted from Deniz Nezza's example:
 *
 * https://blog.eclypse.io/take-photo-or-pick-pictures-with-jetpack-compose-step-by-step-guide-a53cdced7c69, and
 * https://github.com/eclypse-tms/SnapCompose
 */
data class PhotoAlbumViewState(
    val tempPhotoUri: Uri? = null,
    val selectedPhotos: List<ImageBitmap> = emptyList()
) {}