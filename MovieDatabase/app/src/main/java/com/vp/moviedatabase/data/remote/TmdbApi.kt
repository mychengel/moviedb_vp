package com.vp.moviedatabase.data.remote

import com.vp.moviedatabase.data.dto.MovieDetailDto
import com.vp.moviedatabase.data.dto.MovieListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Daftar endpoint TMDB. Semua fungsi `suspend` -> aman dipanggil dari coroutine
 * (Retrofit sendiri yang memindahkan pekerjaan jaringan keluar dari Main thread).
 * Parameter api_key ditambahkan otomatis oleh ApiKeyInterceptor.
 */
interface TmdbApi {

    @GET("movie/popular")
    suspend fun getPopular(@Query("page") page: Int = 1): MovieListResponseDto

    @GET("movie/now_playing")
    suspend fun getNowPlaying(@Query("page") page: Int = 1): MovieListResponseDto

    /** Seluruh data movie (urut populer), mendukung paging lewat parameter page. */
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
    ): MovieListResponseDto

    @GET("movie/{id}")
    suspend fun getMovieDetail(@Path("id") id: Int): MovieDetailDto
}