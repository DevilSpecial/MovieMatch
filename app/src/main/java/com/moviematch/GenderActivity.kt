package com.moviematch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.moviematch.databinding.ActivityGenderBinding

class GenderActivity : AppCompatActivity() {
    val db = Firebase.firestore
    lateinit var userSharedPreferences: SharedPreferences
    lateinit var auth: FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding : ActivityGenderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenderBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        userSharedPreferences = getSharedPreferences("USER", MODE_PRIVATE)
        setContentView(binding.root)
        auth =FirebaseAuth.getInstance()
        val gender = arrayOf("Gender","Male","Female","Others")
        val aa = ArrayAdapter(this, R.layout.custom_spinner_bg, gender)
        aa.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        val username = sharedPreferences.getString("name","user")
        binding.spinner.adapter=aa
        binding.tvSignup.text = "Hello $username,"
        binding.btnOtp.setOnClickListener {
            checkValidity()
        }
    }

    private fun checkValidity() {
        val gender = binding.spinner.selectedItem.toString()
        if(gender=="Gender"){
            fadeInView(binding.tverror)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.tverror)
            }, 1500)
        }else{
            val editor = sharedPreferences.edit()
            editor.putString("gender", gender)
            editor.apply()
            editor.commit()

            addtoDatabase()
            val intent  =Intent(this,MainActivity::class.java)
            startActivity(intent)
            this.overridePendingTransition(R.anim.animate_zoom_enter,R.anim.animate_zoom_exit)
        }
    }

    private fun addtoDatabase() {
        val name = sharedPreferences.getString("name","User")
        val username =  sharedPreferences.getString("username","abcdef")

        val gender = sharedPreferences.getString("gender","Gender")
        val email = sharedPreferences.getString("email","abc@xyz")
        val pw = sharedPreferences.getString("password","123456")
        val user = hashMapOf(
            "Name" to name,
            "Username" to username,
            "Gender" to gender,
            "Email" to email,
            "Password" to pw
        )

        db.collection("Users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                val editor2 = userSharedPreferences.edit()
                editor2.putBoolean("currentUser",true)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                Toast.makeText(this,"Successfully Signed Up",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
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
                    view.visibility = View.INVISIBLE
                }
            })
    }
}