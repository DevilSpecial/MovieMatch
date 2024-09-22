package com.moviematch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.moviematch.databinding.ActivityUserNameBinding
import com.moviematch.utils.ProgressBar

class UserNameActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserNameBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var userSharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        userSharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE)
        binding = ActivityUserNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val name = sharedPreferences.getString("name","user")

        binding.tvSignin.text = "Hello $name,"
    binding.btnNext.setOnClickListener {
        val username = binding.etUsername.text.toString()
        var str =""
        var err = false
        if (username.isEmpty()) {
            str = "**All Fields are required"
            err = true
        }
        if (err) {
            binding.tverror.text=str
            fadeInView(binding.tverror)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.tverror)
            }, 1500)
        }else{
            ProgressBar.showProgressBar(this)
            sendToPhonePage(username)
        }
    }

    }

    private fun sendToPhonePage(username: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .whereEqualTo("Username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {

                    binding.tverror.text = "Username not available."
                    fadeInView(binding.tverror)
                    Handler(Looper.getMainLooper()).postDelayed({
                        fadeOutView(binding.tverror)
                    }, 1500)
                } else {
                    val editor2  =userSharedPreferences.edit()
                    editor2.putString("username",username)
                    editor2.apply()
                    editor2.commit()
                    val editor = sharedPreferences.edit()
                    editor.putString("username", username)
                    editor.apply()
                    editor.commit()
                    ProgressBar.dismissProgressBar()
                    val intent = Intent(this, PasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                    this.overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit)
                }
            }
            .addOnFailureListener { exception ->
                binding.tverror.text = "Error checking username availability. Please try again."
                fadeInView(binding.tverror)
                Handler(Looper.getMainLooper()).postDelayed({
                    fadeOutView(binding.tverror)
                }, 1500)
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