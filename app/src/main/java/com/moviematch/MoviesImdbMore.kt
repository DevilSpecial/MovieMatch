package com.moviematch

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.BlurTransformation
import com.moviematch.TMDBAPI.MovieDescr.MovieDesc
import com.moviematch.databinding.ActivityMoviesImdbMoreBinding
import com.moviematch.network.RetrofitInstance
import com.moviematch.utils.ProgressBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MoviesImdbMore : AppCompatActivity() {
    lateinit var binding : ActivityMoviesImdbMoreBinding
    lateinit var responseBody:MovieDesc

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding= ActivityMoviesImdbMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bndl  = intent.extras
        val id = bndl?.getInt("ID")
//        binding.movieId.text=id.toString()
        binding.btnTrailer.setOnClickListener {
            watchTrailer()
        }
        if (id != null) {
            ProgressBar.showProgressBar(this)
            getData(id.toString())
        } else {

            Toast.makeText(this, "Invalid Movie ID", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
        }
    }

    private fun watchTrailer() {

        val query = "${responseBody.title} trailer Movie ${responseBody.origin_country}"
        val youtubeSearchUrl = "https://www.youtube.com/results?search_query=${Uri.encode(query)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeSearchUrl))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun getData(id : String) {
        val retrofitData = RetrofitInstance.api.getMovieData(id)
        retrofitData.enqueue(object : Callback<MovieDesc> {
            override fun onResponse(call: Call<MovieDesc>, response: Response<MovieDesc>) {
                if (response.isSuccessful && response.body() != null) {

                    responseBody = response.body()!!
                    binding.imvBackdrop.load("https://image.tmdb.org/t/p/w500" + responseBody.backdrop_path) {
                        crossfade(true)
                        transformations(BlurTransformation(this@MoviesImdbMore, 15f))
                    }
                    binding.imvPoster.load("https://image.tmdb.org/t/p/w500" + responseBody.poster_path) {
                        crossfade(true)
                    }
                    setTextViews(responseBody)
                    ProgressBar.dismissProgressBar()
                }else{
                    ProgressBar.dismissProgressBar()
                }
            }

            override fun onFailure(p0: Call<MovieDesc>, p1: Throwable) {
                Toast.makeText(this@MoviesImdbMore,"Error calling",Toast.LENGTH_SHORT).show()
            }
        })
}

    private fun setTextViews(responseBody: MovieDesc) {
        binding.TvTitle.text= responseBody.title
        binding.TvSub.text = "${responseBody.tagline}"
        val date = responseBody.release_date
        val year = date.split("-")[0]
        binding.tvDate.text= year
        binding.tvTime.text = formatMovieTime(responseBody.runtime)
        var genres: String =""
        for (genre in responseBody.genres){
            genres+=genre.name+","
        }
        genres = genres.removeSuffix(",")
        binding.tvGenre.text=genres
        binding.tvBody.text=responseBody.overview
    }
    fun formatMovieTime(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return "$hours h $remainingMinutes min"
    }
}