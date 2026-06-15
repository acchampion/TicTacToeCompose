package edu.osu.tictactoecompose.model.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.model.Intent
import edu.osu.tictactoecompose.model.PhotoAlbumViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.coroutines.CoroutineContext

/**
 * Adapted from Deniz Nezza's example:
 *
 *  https://blog.eclypse.io/take-photo-or-pick-pictures-with-jetpack-compose-step-by-step-guide-a53cdced7c69, and
 *  https://github.com/eclypse-tms/SnapCompose
 */
class PhotoAlbumViewModel(val coroutineContext: CoroutineContext): ViewModel() {

    val _photoAlbumViewState: MutableStateFlow<PhotoAlbumViewState> = MutableStateFlow(PhotoAlbumViewState())
    val photoAlbumViewState: StateFlow<PhotoAlbumViewState>
        get() = _photoAlbumViewState

    private val classTag = javaClass.simpleName

    fun decodeUriAsBitmap(context: Context, uri: Uri): ImageBitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        val imgBytes = inputStream?.readBytes()

        if (imgBytes != null) {
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inMutable = true
            val bitmap: Bitmap =
                BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size, bitmapOptions)
            val imageBitmap = bitmap.asImageBitmap()
            imageBitmap.prepareToDraw()
            return imageBitmap
        }

        inputStream?.close()

        // Error decoding bitmap; return our scoreboard image.
        return ImageBitmap.imageResource(context.resources, R.drawable.scoreboard)
    }

    fun onReceive(intent: Intent) = viewModelScope.launch(coroutineContext) {
        when (intent) {
            is Intent.OnPermissionGrantedWith -> {
                // Create an empty image file in the app's cache directory
                val tempFile = File.createTempFile(
                    "temp_image_file_", /* prefix */
                    ".jpg", /* suffix */
                    intent.compositionContext.cacheDir  /* cache directory */
                )

                // Create sandboxed url for this temp file - needed for the camera API
                val uri = FileProvider.getUriForFile(intent.compositionContext,
                    "${intent.compositionContext.applicationInfo.packageName}.provider", /* needs to match the provider information in the manifest */
                    tempFile
                )

                _photoAlbumViewState.value = _photoAlbumViewState.value.copy(tempPhotoUri = uri)
            }
            is Intent.OnPermissionDenied -> {
                Timber.tag(classTag).e("Error: User denied permission to use camera")
            }
            is Intent.OnFinishPickingImageWith -> {
                if (intent.imageUris.isNotEmpty()) {
                    val newImages = mutableListOf<ImageBitmap>()
                    for (imageUri in intent.imageUris) {
                        val inputStream = intent.compositionContext.contentResolver.openInputStream(imageUri)
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()

                        if (bytes != null) {
                            val bitmapOptions = BitmapFactory.Options()
                            bitmapOptions.inMutable = true
                            val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bitmapOptions)
                            newImages.add(bitmap.asImageBitmap())
                        } else {
                            // error reading the bytes from the image url
                            println("The image that was picked could not be read from the device at this url: $imageUri")
                        }

                        val currentViewState = _photoAlbumViewState.value
                        val newCopy = currentViewState.copy(
                            selectedPhotos = (currentViewState.selectedPhotos + newImages),
                            tempPhotoUri = null
                        )
                        _photoAlbumViewState.value = newCopy
                    }
                } else {
                    Timber.tag(classTag).e("User did not pick anything")
                }
            }
            is Intent.OnImageSavedWith -> {
                val uri = _photoAlbumViewState.value.tempPhotoUri
                if (uri != null) {
                    val tempImageUrl = _photoAlbumViewState.value.tempPhotoUri
                    if (tempImageUrl != null) {
                        val source = ImageDecoder.createSource(intent.compositionContext.contentResolver, tempImageUrl)

                        val currentPhotos = _photoAlbumViewState.value.selectedPhotos.toMutableList()
                        currentPhotos.add(ImageDecoder.decodeBitmap(source).asImageBitmap())

                        _photoAlbumViewState.value = _photoAlbumViewState.value.copy(tempPhotoUri = null,
                            selectedPhotos = currentPhotos)
                    }
                }

            }
            is Intent.OnImageSavingCanceled -> {
                Timber.tag(classTag).i("Canceled saving image")
                _photoAlbumViewState.value = _photoAlbumViewState.value.copy(tempPhotoUri = null)
            }
        }
    }
}