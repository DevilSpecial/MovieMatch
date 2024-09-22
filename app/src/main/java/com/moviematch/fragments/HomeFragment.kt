package com.moviematch.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moviematch.Adapters.TopRatedMoviesAdapter
import com.moviematch.GenreData
import com.moviematch.TMDBAPI.MovieModel.Result
import com.moviematch.TMDBAPI.TVModel.TVDataItem
import com.moviematch.MoviesImdbMore
import com.moviematch.Adapters.PopularTVAdapter
import com.moviematch.SwipeActivity
import com.moviematch.databinding.FragmentHomeBinding
import com.moviematch.network.RetrofitInstance
import com.moviematch.utils.ProgressBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//const val base_url = "https://api.themoviedb.org/3/"
class HomeFragment : Fragment() {
     var totalPages=0
lateinit var binding : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ProgressBar.showProgressBar(requireContext())
        getTopRatedMovies()
        getPopularTV()
        if (GenreData.genreMap.isEmpty()) {
            GenreData.initializeGenres()
        }
        binding.btnTopImdb.setOnClickListener {
            val intent = Intent(requireContext(),SwipeActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getPopularTV() {
        val retrofitData =  RetrofitInstance.api.getTVPopular()
        retrofitData.enqueue(object : Callback<TVDataItem>{
            override fun onResponse(p0: Call<TVDataItem>, p1: Response<TVDataItem>) {
                if(p1.isSuccessful && p1.body()!=null){
                    val responseBody = p1.body()!!.results
                    binding.RCV2.layoutManager= LinearLayoutManager(context)
                    binding.RCV2.adapter = PopularTVAdapter(responseBody,requireContext())


                }
            }

            override fun onFailure(p0: Call<TVDataItem>, p1: Throwable) {
                Log.e("RetrofitError", "Request failed: ${p1.message}", p1)
                ProgressBar.dismissProgressBar()
                Toast.makeText(requireContext(),"Cant get movies",Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getTopRatedMovies() {
        val retrofitData = RetrofitInstance.api.getTopRatedMovies()
        retrofitData.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                        if (response.isSuccessful && response.body() != null) {
                            totalPages = response.body()!!.total_pages
                            val responseBody = response.body()!!.results
                            binding.RCV.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
                            val adapter = TopRatedMoviesAdapter(responseBody,requireContext())
                            binding.RCV.adapter=adapter
                            ProgressBar.dismissProgressBar()

                        }
                    }
                    override fun onFailure(call: Call<Result>, t: Throwable) {
                        ProgressBar.dismissProgressBar()
                    }
                })

    }

}