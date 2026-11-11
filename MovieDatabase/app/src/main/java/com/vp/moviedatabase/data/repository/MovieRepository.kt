package com.vp.moviedatabase.data.repository

import com.vp.moviedatabase.BuildConfig
import com.vp.moviedatabase.data.remote.TmdbApi
import com.vp.moviedatabase.model.MovieCategory
import com.vp.moviedatabase.model.MovieDetail
import com.vp.moviedatabase.model.MoviePage
import com.vp.moviedatabase.util.safeCall

class MovieRepository(private val api: TmdbApi) {

    /** Movie untuk carousel horizontal (Popular / Now Playing). */
    suspend fun getFeatured(category: MovieCategory): Result<MoviePage> = guarded {
        when (category) {
            MovieCategory.POPULAR -> api.getPopular()
            MovieCategory.NOW_PLAYING -> api.getNowPlaying()
        }.toDomain()
    }

    /** Seluruh movie (list vertikal), per halaman. */
    suspend fun getAllMovies(page: Int): Result<MoviePage> = guarded {
        api.discoverMovies(page).toDomain()
    }

    suspend fun getMovieDetail(id: Int): Result<MovieDetail> = guarded {
        api.getMovieDetail(id).toDomain()
    }

    private suspend fun <T> guarded(block: suspend () -> T): Result<T> {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("TMDB_API_KEY belum diisi. Tambahkan di local.properties lalu sync Gradle.")
            )
        }
        return safeCall(block)
    }
}