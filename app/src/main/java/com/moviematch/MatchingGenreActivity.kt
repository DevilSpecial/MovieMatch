package com.moviematch

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moviematch.Adapters.CardStackMovieAdapter
import com.moviematch.Adapters.GenreMovieAdapter
import com.moviematch.TMDBAPI.MovieModel.Result
import com.moviematch.databinding.ActivityMatchingGenreBinding
import com.moviematch.network.RetrofitInstance
import com.moviematch.tempActivity.RoomData
import com.moviematch.utils.ProgressBar
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.Duration
import com.yuyakaido.android.cardstackview.StackFrom
import com.yuyakaido.android.cardstackview.*
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MatchingGenreActivity : AppCompatActivity(),CardStackListener {
    var totalPages=0
    val database = Firebase.database
    lateinit var userSharedPreferences: SharedPreferences
    var genreId: String?=null
    var pw :String?=null
    lateinit var binding : ActivityMatchingGenreBinding
    lateinit var cardStackMovieAdapter: CardStackMovieAdapter
    lateinit var cardStackLayoutManager: CardStackLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userSharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE)
        binding = ActivityMatchingGenreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cardStackMovieAdapter = CardStackMovieAdapter(emptyList(), this)
        val bndl =intent.extras
        val genre = bndl?.getStringArrayList("SelectedGenres")
        pw = bndl?.getString("roomId")
        Toast.makeText(this,pw,Toast.LENGTH_SHORT).show()
        binding.tvGenres.text = genre?.joinToString(", ")
        val selectedGenres = bndl!!.getStringArrayList("SelectedGenres")
        val genreIds = selectedGenres?.mapNotNull { genreName ->
            GenreData.genreMap.entries.firstOrNull { it.value == genreName }?.key
        }
        genreId = genreIds?.joinToString("|")
        getGenreMovies(genreId)
        cardStackLayoutManager = CardStackLayoutManager(this,this)
        cardStackLayoutManager.setStackFrom(StackFrom.Top)
        cardStackLayoutManager.setCanScrollHorizontal(true)


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

    }

    private fun sendGenretoDB(pw:String?) {
        val roomRef = database.getReference("room").child(pw.toString())
        roomRef.get().addOnSuccessListener { dataSnapshot ->
            roomRef.child("genres").setValue(genreId)
        }.addOnFailureListener {exception ->
            Log.e("FirebaseError", "Failed to send genre to DB", exception)
            Toast.makeText(this, "Room not found or other error.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {

    }

    override fun onCardSwiped(direction: Direction) {
        val currUser = userSharedPreferences.getString("username", "User2")
        val currentPosition = cardStackLayoutManager.topPosition - 1
        val swipedMovie = (binding.cardStack.adapter as CardStackMovieAdapter).getMovieAtPosition(currentPosition)
        val roomRef = Firebase.database.getReference("room").child(pw.toString())
        roomRef.get().addOnSuccessListener { dataSnapshot ->
            val roomData = dataSnapshot.getValue(RoomData::class.java)
//        Toast.makeText(this,roomData?.guest,Toast.LENGTH_SHORT).show()
        when (direction) {
            Direction.Right -> {
                if (currUser == roomData?.hostId) {
                    Log.d("Movie host", swipedMovie.title.toString())
                    roomRef.child("likedMovies").child("hostLikes").push().setValue(swipedMovie.title)
                } else if (currUser == roomData?.guest) {
                    Log.d("Movie guest", swipedMovie.title.toString())
                    roomRef.child("likedMovies").child("guestLikes").push().setValue(swipedMovie.title)
                }
            }
            Direction.Left -> {
                // Log the movie title when swiped left
                Log.d("Movie", "Left swipe: ${swipedMovie.title.toString()}")
            }
            else -> {
                // Handle other cases if necessary
            }
        }}

    }

    override fun onCardRewound() {
        val currentPosition = cardStackLayoutManager.topPosition
        val rewoundMovie = (binding.cardStack.adapter as CardStackMovieAdapter).getMovieAtPosition(currentPosition)

        Log.d("Movie",rewoundMovie.title.toString())
    }

    override fun onCardCanceled() {

    }

    override fun onCardAppeared(view: View?, position: Int) {

    }

    override fun onCardDisappeared(view: View?, position: Int) {

    }

    private fun getGenreMovies(genreId: String?) {
        ProgressBar.showProgressBar(this)
        val retrofitData = RetrofitInstance.api.getGenreMovieData(genre = genreId.toString())
        retrofitData.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                val requestUrl = retrofitData.request().url().toString()
                Log.d("API_REQUEST", "Request URL: ${requestUrl}")
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.results
                    responseBody.shuffled()
                    binding.cardStack.layoutManager = cardStackLayoutManager
//                    cardStackLayoutManager.setVisibleCount(1)
                    cardStackLayoutManager.setCanScrollHorizontal(true)
                    cardStackLayoutManager.setStackFrom(StackFrom.Top)
                    val adapter = CardStackMovieAdapter(responseBody, this@MatchingGenreActivity)
                    binding.cardStack.adapter = adapter
                    ProgressBar.dismissProgressBar()
                }
                else
                    Toast.makeText(this@MatchingGenreActivity,"No response hit",Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Result>, t: Throwable) {

                ProgressBar.dismissProgressBar()
            }
        })

    }
}