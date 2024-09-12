package com.moviematch.Adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.moviematch.TMDBAPI.TVModel.TVInfo
import com.moviematch.TVDescActivity
import com.moviematch.databinding.CardGenreBinding
import com.moviematch.databinding.PopularTvCardBinding
import com.moviematch.getGenreNames
import com.moviematch.getGenreNamesString

class PopularTVAdapter(private var data: List<TVInfo>, var cont: Context) :
    RecyclerView.Adapter<PopularTVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PopularTvCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding,cont)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val TVInfo = data[position]
        holder.bind(TVInfo)
    }

    override fun getItemCount(): Int {
        return data.count()
    }
    fun updateData(newData: List<TVInfo>) {
        data = newData
        notifyDataSetChanged()
    }
    class ViewHolder(private val binding: PopularTvCardBinding,var cont: Context) : RecyclerView.ViewHolder(binding.root) {
        fun bind(TvItem: TVInfo) {
            binding.ivPoster.load("https://image.tmdb.org/t/p/w300" + TvItem.poster_path) {
                crossfade(true)
            }
            val genreList = TvItem?.genre_ids
            val genreNamesString = getGenreNamesString(genreList)
            binding.tvTitle.text = TvItem.name
            binding.tvDate.text  = "Release Data : "+TvItem.first_air_date
            binding.rvGenre.layoutManager= LinearLayoutManager(cont,RecyclerView.HORIZONTAL,false)
            val adapter = GenreAdapter(getGenreNames(genreList))
            binding.rvGenre.adapter  =adapter

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt("ID", TvItem.id!!)
                val intent = Intent(cont, TVDescActivity::class.java)
                intent.putExtras(bundle)
                cont.startActivity(intent)
            }
        }
    }
}




class GenreAdapter(private  var data: List<String>): RecyclerView.Adapter<GenreAdapter.ViewHolder>(){
    class ViewHolder(private val binding: CardGenreBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(genre : String) {
            binding.tvGenre.text=genre
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        if(data.count()>3) return 3
       return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = data[position]
        holder.bind(genre)
    }
}