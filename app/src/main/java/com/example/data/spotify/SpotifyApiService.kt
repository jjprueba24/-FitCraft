package com.example.data.spotify

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class SpotifyTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "scope") val scope: String? = null
)

data class SpotifyUserProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "display_name") val displayName: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "product") val product: String? // "premium", "free", "open"
)

data class SpotifyPlayRequest(
    @Json(name = "context_uri") val contextUri: String? = null,
    @Json(name = "uris") val uris: List<String>? = null
)

data class SpotifyPlaylistDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "uri") val uri: String
)

interface SpotifyAccountsApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun exchangeCodeForToken(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String
    ): Response<SpotifyTokenResponse>

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshAccessToken(
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Response<SpotifyTokenResponse>
}

interface SpotifyWebApi {
    @GET("v1/me")
    suspend fun getCurrentUser(
        @Header("Authorization") bearerToken: String
    ): Response<SpotifyUserProfileDto>

    @GET("v1/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Header("Authorization") bearerToken: String,
        @Path("playlist_id") playlistId: String
    ): Response<SpotifyPlaylistDto>

    @PUT("v1/me/player/play")
    suspend fun startOrResumePlayback(
        @Header("Authorization") bearerToken: String,
        @Body playRequest: SpotifyPlayRequest
    ): Response<Unit>

    @PUT("v1/me/player/pause")
    suspend fun pausePlayback(
        @Header("Authorization") bearerToken: String
    ): Response<Unit>

    @POST("v1/me/player/next")
    suspend fun skipToNext(
        @Header("Authorization") bearerToken: String
    ): Response<Unit>

    @POST("v1/me/player/previous")
    suspend fun skipToPrevious(
        @Header("Authorization") bearerToken: String
    ): Response<Unit>
}

object SpotifyNetworkModule {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val accountsApi: SpotifyAccountsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyAccountsApi::class.java)
    }

    val webApi: SpotifyWebApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyWebApi::class.java)
    }
}
