package com.vp.moviedatabase.data.remote

import com.vp.moviedatabase.BuildConfig
import com.vp.moviedatabase.data.remote.ApiKeyInterceptor
import com.vp.moviedatabase.data.remote.TmdbApi
import com.vp.moviedatabase.data.repository.MovieRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Tempat membuat objek-objek singleton (Retrofit, Repository) secara manual.
 * Pada project besar bagian ini biasanya diganti Hilt/Koin.
 */
object ServiceLocator {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    private val json = Json { ignoreUnknownKeys = true } // abaikan field baru dari server

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.TMDB_API_KEY))
            // BASIC saja: level BODY/URL penuh akan menampilkan api_key di Logcat
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    private val api: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
    }

    val movieRepository: MovieRepository by lazy { MovieRepository(api) }
}