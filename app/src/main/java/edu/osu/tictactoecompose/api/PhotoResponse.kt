package edu.osu.tictactoecompose.api

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import edu.osu.tictactoecompose.model.GalleryItem

@Keep
@JsonClass(generateAdapter = true)
class PhotoResponse {
    @Json(name = "photo")
    lateinit var galleryItems: List<GalleryItem>

}
