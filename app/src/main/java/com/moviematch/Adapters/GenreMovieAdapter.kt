package com.moviematch.Adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.moviematch.Adapters.TopRatedMoviesAdapter.ViewHolder
import com.moviematch.MoviesImdbMore
import com.moviematch.TMDBAPI.MovieModel.MovieDataItem
import com.moviematch.databinding.PopularMovieCardBinding
import com.moviematch.getGenreNamesString

class GenreMovieAdapter(private var data: List<MovieDataItem>, var cont: Context) : RecyclerView.Adapter<GenreMovieAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PopularMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, cont)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movieDataItem = data[position]
        holder.bind(movieDataItem)
    }

    override fun getItemCount(): Int {
        return data.count()
    }


    class ViewHolder(private val binding: PopularMovieCardBinding,var cont: Context) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movieDataItem: MovieDataItem) {
            binding.ivPoster.load("https://image.tmdb.org/t/p/w300" + movieDataItem.poster_path) {
                crossfade(true)
            }
            val genreList = movieDataItem?.genre_ids
            val genreNamesString = getGenreNamesString(genreList)
            binding.tvTitle.text = movieDataItem.title
            binding.tvYear.text  = genreNamesString
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("ID", movieDataItem.id!!)
                val intent = Intent(cont, MoviesImdbMore::class.java)
                intent.putExtras(bundle)
                cont.startActivity(intent)
            }
        }
}}