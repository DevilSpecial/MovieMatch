package com.moviematch.TMDBAPI.GenreMovieData

data class GenreMovieData(
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
)