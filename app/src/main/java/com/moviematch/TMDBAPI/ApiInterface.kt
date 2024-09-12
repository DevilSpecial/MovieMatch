package com.moviematch.TMDBAPI

import com.moviematch.TMDBAPI.MovieDescr.MovieDesc
import com.moviematch.TMDBAPI.MovieModel.Result
import com.moviematch.TMDBAPI.TVDescr.TVDesc
import com.moviematch.TMDBAPI.TVModel.TVDataItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @GET("movie/top_rated?api_key=d1b5e8b2356cd6d6105b38a9184615db")
    fun getTopRatedMovies(): Call<Result>


    @GET("discover/tv?api_key=d1b5e8b2356cd6d6105b38a9184615db")
    fun getTVPopular(): Call<TVDataItem>

    @GET("movie/{id}?api_key=d1b5e8b2356cd6d6105b38a9184615db")
    fun getMovieData(@Path("id") id: String): Call<MovieDesc>

    @GET("tv/{id}?api_key=d1b5e8b2356cd6d6105b38a9184615db")
    fun getTVData(@Path("id") id : String): Call<TVDesc>

    @GET("discover/movie")
    fun getGenreMovieData(
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("api_key") apiKey: String = "d1b5e8b2356cd6d6105b38a9184615db",
        @Query("with_genres") genre: String
    ): Call<Result>

}