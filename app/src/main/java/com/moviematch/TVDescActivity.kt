package com.moviematch

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import coil.transform.BlurTransformation
import com.moviematch.TMDBAPI.MovieDescr.MovieDesc
import com.moviematch.TMDBAPI.TVDescr.TVDesc
import com.moviematch.databinding.ActivityTvdescBinding
import com.moviematch.network.RetrofitInstance
import com.moviematch.utils.ProgressBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TVDescActivity : AppCompatActivity() {
    lateinit var  binding : ActivityTvdescBinding

    lateinit var responseBody: TVDesc
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTvdescBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bndl  = intent.extras
        val id = bndl?.getInt("ID")
        binding.btnTrailer.setOnClickListener {
            watchTrailer()
        }
        if (id != null) {
            ProgressBar.showProgressBar(this)
            getData(id.toString())
        } else {
            // Handle the case where ID is null, perhaps finish the activity or show an error message
            Toast.makeText(this, "Invalid Movie ID", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        }

    }

    private fun watchTrailer() {

        val query = "${responseBody.name} trailer TV SERIES ${responseBody.origin_country}"
        val youtubeSearchUrl = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeSearchUrl))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }else{Toast.makeText(this@TVDescActivity,"Error",Toast.LENGTH_SHORT).show()}
    }
    private fun getData(id: String) {
        val retrofitData = RetrofitInstance.api.getTVData(id)
        retrofitData.enqueue(object: Callback<TVDesc>{
            override fun onResponse(p0: Call<TVDesc>, response: Response<TVDesc>) {
                if (response.isSuccessful && response.body() != null) {

                    responseBody = response.body()!!
                    binding.imvBackdrop.load("https://image.tmdb.org/t/p/w500" + responseBody.backdrop_path) {
                        crossfade(true)
                        transformations(BlurTransformation(this@TVDescActivity, 15f))
                    }
                    binding.imvPoster.load("https://image.tmdb.org/t/p/w500" + responseBody.poster_path) {
                        crossfade(true)
                    }
                    setTextViews(responseBody)
                    ProgressBar.dismissProgressBar()
                }else{

                }
            }

            override fun onFailure(p0: Call<TVDesc>, p1: Throwable) {
                ProgressBar.dismissProgressBar()
            }
        })

    }

    private fun setTextViews(responseBody: TVDesc) {
        binding.TvTitle.text= responseBody.name

        binding.tvDate.text=responseBody.last_air_date

        binding.tvTime.text = responseBody.number_of_seasons.toString()
        var genres =""
        for (genre in responseBody.genres){
            genres+=genre.name+","
        }
        genres = genres.removeSuffix(",")
        binding.tvGenre.text=genres
        binding.tvBody.text=responseBody.overview
    }
}