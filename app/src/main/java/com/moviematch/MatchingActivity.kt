package com.moviematch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moviematch.databinding.ActivityMatchingBinding


class Matching : AppCompatActivity() {
    private var selectedChipCount = 0
    private val maxSelectionCount = 3
    val database = Firebase.database
    var pw:String? = null
    lateinit var binding : ActivityMatchingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMatchingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        GenreData.initializeGenres()
        GetTheChips()
        val bndl =intent.extras
        pw = bndl?.getString("roomId")

        binding.btnSubmit.setOnClickListener {
            sendData()
        }
    }

    private fun sendData() {
        val selectedGenres = mutableListOf<String>()

        // Iterate through the chips and collect the selected ones
        binding.genreChipGroup.children.forEach { view ->
            if (view is Chip && view.isChecked) {
                selectedGenres.add(view.text.toString())
            }
        }

        if (selectedGenres.isNotEmpty()) {
            // Create an intent to start the next activity
            val intent = Intent(this, MatchingGenreActivity::class.java)
            intent.putExtra("roomId",pw)
            Toast.makeText(this@Matching,selectedGenres.toString(),Toast.LENGTH_SHORT).show()
            sendGenretoDB(selectedGenres)
            intent.putStringArrayListExtra("SelectedGenres", ArrayList(selectedGenres))
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please select at least one genre.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun sendGenretoDB(selectedGenres: MutableList<String>) {
        val roomRef = database.getReference("room").child(pw.toString())
        roomRef.get().addOnSuccessListener { dataSnapshot ->
            roomRef.child("genres").setValue(selectedGenres.toString())
        }.addOnFailureListener {exception ->
            Log.e("FirebaseError", "Failed to send genre to DB", exception)
            Toast.makeText(this, "Room not found or other error.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun GetTheChips() {
        GenreData.genreMap.forEach { (genreId, genreName) ->
            val chip = Chip(this).apply {
                text = genreName
                isCheckable = true
                
                setChipBackgroundColorResource(R.color.genre_bg)  // Set the selector for chip background
                typeface = ResourcesCompat.getFont(this@Matching, R.font.poppins)
                setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (selectedChipCount == 0) {
                            fadeInView(binding.btnSubmit)
                        }
                        if (selectedChipCount < maxSelectionCount) {
                            selectedChipCount++
                        } else {
                            Toast.makeText(this@Matching,"Limit reached, uncheck to add more Genres",Toast.LENGTH_SHORT).show()
                            buttonView.isChecked = false
                        }
                    } else {
                        selectedChipCount--
                        if (selectedChipCount == 0) {
                            fadeOutView(binding.btnSubmit)
                        }

                    }
                    updateChipStates()
                }
            }
            binding.genreChipGroup.addView(chip)
    }}
    private fun updateChipStates() {
        val children = binding.genreChipGroup.children
        children.forEach { view ->
            if (view is Chip) {
                view.isEnabled = selectedChipCount < maxSelectionCount || view.isChecked
            }
        }}
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun fadeInView(view: View, duration: Long = 500) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null)
    }

    fun fadeOutView(view: View, duration: Long = 500) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.INVISIBLE
                }
            })
    }
}