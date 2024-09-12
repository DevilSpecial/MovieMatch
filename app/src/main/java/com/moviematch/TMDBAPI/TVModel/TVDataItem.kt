package com.moviematch.TMDBAPI.TVModel

data class TVDataItem(
    val page: Int,
    val results: List<TVInfo>,
    val total_pages: Int,
    val total_results: Int
)