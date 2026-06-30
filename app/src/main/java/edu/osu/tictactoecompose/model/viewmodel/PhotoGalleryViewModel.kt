package edu.osu.tictactoecompose.model.viewmodel

import android.app.Application
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.wiley.fordummies.androidsdk.tictactoe.model.Settings
import edu.osu.tictactoecompose.R
import edu.osu.tictactoecompose.TicTacToeApplication
import edu.osu.tictactoecompose.api.FlickrApi
import edu.osu.tictactoecompose.api.FlickrResponse
import edu.osu.tictactoecompose.uistate.PhotoGalleryUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.IOException
import timber.log.Timber

@Keep
class PhotoGalleryViewModel(app: Application) : AndroidViewModel(app) {
    //	val galleryItemLiveData: LiveData<List<GalleryItem>>

    var photoGalleryUiState: PhotoGalleryUiState by mutableStateOf(PhotoGalleryUiState.Loading)

    private val dataStore = (app as TicTacToeApplication).dataStore
    private val mutableSearchTerm = MutableLiveData<String>()
    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""
    private val classTag = javaClass.simpleName

    @JvmOverloads
    fun fetchPhotos(query: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.putString(Settings.Keys.PREF_SEARCH_QUERY, "")
        }
        mutableSearchTerm.value = query
    }

    init {
        Timber.tag(classTag).i("Initializing ViewModel")

        fetchFlickrPhotos()

        Timber.tag(classTag).i("Finished initializer")
    }

    private fun fetchFlickrPhotos() {
        viewModelScope.launch {
            photoGalleryUiState = try {
                val accessToken = application.applicationContext.resources.getString(R.string.flickr_access_token)

                val flickrResponse: FlickrResponse =
                    FlickrApi.retrofitService.fetchPhotos(accessToken)
                Timber.tag(classTag).d("Response received")

                val photoResponse = flickrResponse.photos
                val galleryItems = photoResponse.galleryItems
                PhotoGalleryUiState.Success(galleryItems)
                //_galleryItemState = MutableStateFlow(galleryItems)
                //galleryItemState = _galleryItemState.asStateFlow()
            } catch (e: IOException) {
                Timber.tag("PhotoGalleryViewModel coroutine").e("Could not load photos")
                PhotoGalleryUiState.Error
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    class PhotoGalleryViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PhotoGalleryViewModel::class.java)) {
                return PhotoGalleryViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
