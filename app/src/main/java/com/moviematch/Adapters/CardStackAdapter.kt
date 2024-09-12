package com.moviematch.Adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.moviematch.MoviesImdbMore
import com.moviematch.TMDBAPI.MovieModel.MovieDataItem
import com.moviematch.databinding.PopularMovieCardBinding
import com.moviematch.databinding.SwipeMovieCardBinding
import com.moviematch.getGenreNamesString

class CardStackMovieAdapter(private var data: List<MovieDataItem>, private val context: Context) : RecyclerView.Adapter<CardStackMovieAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SwipeMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movieDataItem = data[position]
        holder.bind(movieDataItem)
    }

    override fun getItemCount(): Int {
        return data.count()
    }
    fun getMovieAtPosition(position: Int): MovieDataItem {
        return data[position]
    }

    class ViewHolder(private val binding: SwipeMovieCardBinding, private val context: Context) : RecyclerView.ViewHolder(binding.root) {
        fun bind(movieDataItem: MovieDataItem) {
            binding.ivPoster.load("https://image.tmdb.org/t/p/w300" + movieDataItem.poster_path) {
                crossfade(true)
            }
            val genreList = movieDataItem.genre_ids
            val genreNamesString = getGenreNamesString(genreList)
            binding.tvTitle.text = movieDataItem.title
            binding.tvGenre.text = genreNamesString
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("ID", movieDataItem.id!!)
                val intent = Intent(context, MoviesImdbMore::class.java)
                intent.putExtras(bundle)
                context.startActivity(intent)
            }
        }
    }
}
