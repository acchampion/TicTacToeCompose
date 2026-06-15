package edu.osu.tictactoecompose.model

import android.content.Context
import android.net.Uri

sealed class Intent {
    data class OnPermissionGrantedWith(val compositionContext: Context): Intent()
    data object OnPermissionDenied: Intent()
    data class OnImageSavedWith (val compositionContext: Context): Intent()
    data object OnImageSavingCanceled: Intent()
    data class OnFinishPickingImageWith(val compositionContext: Context, val imageUris: List<Uri>): Intent()
}