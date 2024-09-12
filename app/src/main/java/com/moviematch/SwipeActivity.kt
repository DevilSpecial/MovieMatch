package com.moviematch

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.moviematch.Adapters.CardStackMovieAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.moviematch.databinding.ActivitySwipeBinding
import com.moviematch.network.RetrofitInstance
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting

class SwipeActivity : AppCompatActivity() {
    lateinit var binding : ActivitySwipeBinding
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding=ActivitySwipeBinding.inflate(layoutInflater)
        // Setup CardStackLayoutManager
        cardStackLayoutManager = CardStackLayoutManager(this)
        cardStackLayoutManager.setStackFrom(StackFrom.Top)
        cardStackLayoutManager.setCanScrollHorizontal(true)
        cardStackLayoutManager.setCanScrollVertical(false)

        binding.cardStack.layoutManager = cardStackLayoutManager

        binding.btnRewind.setOnClickListener {
            binding.cardStack.rewind()
        }

        binding.btnRight.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build()
            cardStackLayoutManager.setSwipeAnimationSetting(setting)
            binding.cardStack.swipe()
        }

        binding.btnLeft.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .build()
            cardStackLayoutManager.setSwipeAnimationSetting(setting)
            binding.cardStack.swipe()
        }
        val retrofitData = RetrofitInstance.api.getTopRatedMovies()
        retrofitData.enqueue(object : Callback<com.moviematch.TMDBAPI.MovieModel.Result> {
            override fun onResponse(call: Call<com.moviematch.TMDBAPI.MovieModel.Result>, response: Response<com.moviematch.TMDBAPI.MovieModel.Result>) {
                val requestUrl = retrofitData.request().url().toString()
                Log.d("API_REQUEST", "Request URL: $requestUrl")
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@SwipeActivity, "Successful hit", Toast.LENGTH_SHORT).show()
                    val responseBody = response.body()!!.results

                    binding.cardStack.layoutManager = cardStackLayoutManager
//                    cardStackLayoutManager.setVisibleCount(1)
                    cardStackLayoutManager.setCanScrollHorizontal(true)
                    cardStackLayoutManager.setStackFrom(StackFrom.Top)

                    cardStackLayoutManager.setCanScrollVertical(false)
                    val adapter = CardStackMovieAdapter(responseBody, this@SwipeActivity)
                    binding.cardStack.adapter = adapter


                } else {
                    Toast.makeText(this@SwipeActivity, "No response hit", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<com.moviematch.TMDBAPI.MovieModel.Result>, t: Throwable) {
                Toast.makeText(this@SwipeActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

    }
}