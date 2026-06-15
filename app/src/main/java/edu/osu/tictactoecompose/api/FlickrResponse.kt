package edu.osu.tictactoecompose.api

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
class FlickrResponse {
    @Json(name = "photos")
    lateinit var photos: PhotoResponse

}
