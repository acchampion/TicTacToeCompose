package edu.osu.tictactoecompose.api

import androidx.annotation.Keep
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

@Keep
interface FlickrApiOld {
    @GET("/")
    fun fetchContents(): Call<String>

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    suspend fun fetchPhotos(@Query("api_key") query: String): Call<FlickrResponse>

    @GET
    suspend fun fetchUrlBytes(@Url url: String?): Call<ResponseBody>

    @GET("services/rest/?method=flickr.photos.search")
    suspend fun searchPhotos(@Query("text") query: String): Call<FlickrResponse>
}
