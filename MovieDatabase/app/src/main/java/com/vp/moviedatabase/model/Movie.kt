package com.vp.moviedatabase.model

/** Model untuk daftar movie (dipakai UI). */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseYear: String,
    val rating: Double,
)

/** Model untuk halaman detail. */
data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String,
    val overview: String,
    val runtimeMinutes: Int?,
    val genres: List<String>,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val rating: Double,
    val voteCount: Int,
)

data class Genre(
    val id: Int,
    val name: String,
)

/** Satu halaman hasil dari API. */
data class MoviePage(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
) {
    val isLastPage: Boolean get() = page >= totalPages
}

enum class MovieCategory(val label: String) {
    POPULAR("Popular"),
    NOW_PLAYING("Now Playing"),
}