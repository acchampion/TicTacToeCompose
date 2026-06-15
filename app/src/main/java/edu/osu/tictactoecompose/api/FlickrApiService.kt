package edu.osu.tictactoecompose.api

import androidx.annotation.Keep
import edu.osu.tictactoecompose.R
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

private val classTag = "FlickrApiService"

@Keep
interface FlickrApiService {
    @GET("/")
    suspend fun fetchContents(): Call<String>

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    suspend fun fetchPhotos(@Query("api_key") query: String): FlickrResponse

    @GET
    suspend fun fetchUrlBytes(@Url url: String?): Call<ResponseBody>

    @GET("services/rest/?method=flickr.photos.search")
    suspend fun searchPhotos(@Query("text") query: String): FlickrResponse
}

private val photoInterceptor: PhotoInterceptor = PhotoInterceptor()
private val client: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(photoInterceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl("https://api.flickr.com/")
    .addConverterFactory(MoshiConverterFactory.create())
    .client(client)
    .build()

object FlickrApi {

    private val flickrAccessToken = R.string.flickr_access_token

    val retrofitService: FlickrApiService by lazy {
        retrofit.create(FlickrApiService::class.java)
    }
}
