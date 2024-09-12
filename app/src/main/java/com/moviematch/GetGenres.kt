package com.moviematch

fun getGenreNamesString(genreIds: List<Int>?): String {
    val genreNames = genreIds!!.mapNotNull { GenreData.genreMap[it] }
    val displayedGenres = if (genreNames.size > 3) {
        genreNames.take(3) + "..."
    } else {
        genreNames
    }
    return genreNames!!.joinToString(separator = ", ")
}
fun getGenreNames(genreIds: List<Int>?): List<String> {
    return genreIds?.mapNotNull { GenreData.genreMap[it] } ?: emptyList()
}