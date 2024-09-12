package com.moviematch.TMDBAPI.MovieModel

data class Result(
    val page: Int,
    val results: List<MovieDataItem>,
    val total_pages: Int,
    val total_results: Int
)
