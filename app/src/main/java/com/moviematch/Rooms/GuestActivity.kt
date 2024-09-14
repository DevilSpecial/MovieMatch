package com.moviematch.Rooms

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moviematch.MatchingGenreActivity
import com.moviematch.R
import com.moviematch.databinding.ActivityGuestBinding
import com.moviematch.tempActivity.RoomData
import com.moviematch.utils.ProgressBar

class GuestActivity : AppCompatActivity() {
    lateinit  var password :String
    lateinit var userSharedPreferences: SharedPreferences
    val database = Firebase.database
    lateinit var binding: ActivityGuestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        password="123456"
        super.onCreate(savedInstanceState)
      binding= ActivityGuestBinding.inflate(layoutInflater)
        userSharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE)
        setContentView(binding.root)

        val editTextList = listOf(
            binding.et1,
            binding.et2,
            binding.et3,
            binding.et4,
            binding.et5,
            binding.et6
        )
        for (i in editTextList.indices) {
            editTextList[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        // Move focus to the next EditText if current one is filled
                        if (i < editTextList.size - 1) {
                            editTextList[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0 && i > 0) {
                        // Move focus to the previous EditText when deleted
                        editTextList[i - 1].requestFocus()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
        binding.btnJoin.setOnClickListener {
            val editTextList = listOf(
                binding.et1, binding.et2, binding.et3,
                binding.et4, binding.et5, binding.et6
            )

            // Check if all EditTexts have exactly one digit
            var allFieldsFilled = true
            for (editText in editTextList) {
                if (editText.text.toString().trim().isEmpty()) {
                    allFieldsFilled = false
                    break
                }
            }
            if (allFieldsFilled) {
                // Combine digits if all fields are filled
                val combinedNumber = StringBuilder()
                for (editText in editTextList) {
                    combinedNumber.append(editText.text.toString())
                }
                password=combinedNumber.toString()
                startCheckingRoom()
        }else {
                binding.tvError.text = "*Enter a 6 digit Number"
                fadeInView(binding.tvError)
                Handler(Looper.getMainLooper()).postDelayed({
                    fadeOutView(binding.tvError)
                }, 1500)
        }

    }
    }

    private fun startCheckingRoom() {
        ProgressBar.showProgressBar(this)
        val roomRef = database.getReference("room").child(password)
        roomRef.get().addOnSuccessListener { dataSnapshot ->
            val roomData = dataSnapshot.getValue(RoomData::class.java)
            val username  = userSharedPreferences.getString("username","User2")
            if (roomData != null) {

                roomRef.child("guest").setValue(username)
                val hostid = roomData.hostId
                binding.tvHost.text = "You have joined the room of $hostid"
                binding.tvHost.visibility =View.VISIBLE
                binding.btnJoin.visibility = View.GONE
                binding.et1.visibility=View.GONE
                binding.et2.visibility=View.GONE
                binding.et3.visibility=View.GONE
                binding.et4.visibility=View.GONE
                binding.et5.visibility=View.GONE
                binding.et6.visibility=View.GONE
                binding.tvWait.visibility=View.VISIBLE
                ProgressBar.dismissProgressBar()
                roomRef.child("genres").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val genre = dataSnapshot.getValue(String::class.java)
                        if (genre != null) {
                            val final = genre.removeSurrounding("[","]")
                            binding.tvWait.text = "$hostid has chosen the genre/s : $final"
                            binding.btnSwipe.isEnabled = true
                            binding.btnSwipe.visibility = View.VISIBLE
                            binding.btnSwipe.setOnClickListener {
                                val selectedGenres = genre
                                    .removeSurrounding("[", "]")  // Removes the surrounding square brackets
                                    .split(",")                    // Splits the string into a list
                                    .map { it.trim() }              // Trims any extra spaces
                                    .toMutableList()
                                val intent = Intent(this@GuestActivity, MatchingGenreActivity::class.java)
                                intent.putStringArrayListExtra("SelectedGenres", ArrayList(selectedGenres))
                                startActivity(intent)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@GuestActivity, "Failed to load genre", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
ProgressBar.dismissProgressBar()
                binding.tvError.text = "Invalid password, Make sure you entered te right password"
                fadeInView(binding.tvError)
                Handler(Looper.getMainLooper()).postDelayed({
                    fadeOutView(binding.tvError)
                }, 3000)

            }
        }.addOnFailureListener {
            ProgressBar.dismissProgressBar()
            Toast.makeText(this, "Room not found or other error.", Toast.LENGTH_SHORT).show()
        }
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
                    view.visibility = View.GONE
                }
            })
    }
}
