package com.moviematch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.moviematch.databinding.PasswordActivityBinding

class PasswordActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: PasswordActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        binding= PasswordActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val username = sharedPreferences.getString("name","user")
        auth=FirebaseAuth.getInstance()

        binding.tvSignup.text = "Hello $username,"
        binding.btnOtp.setOnClickListener {
            checkValidity()
        }
        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.etPw2.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.etCpw.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Hide password
                binding.etPw2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.etCpw.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            binding.etPw2.setSelection(binding.etPw2.text?.length ?: 0)
            binding.etCpw.setSelection(binding.etPw2.text?.length ?: 0)
        }}



    private fun checkValidity() {
        val pw = binding.etPw2.text.toString()
        var str = " "
        var  err: Boolean = false
        var  err2: Boolean=false
        val pw2 = binding.etCpw.text.toString()
        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{6,}$"
        if ( pw.isEmpty() || pw2.isEmpty()){
            str = "**All Fields are required"
            err = true
        }else if(pw!=pw2){
            err2=true
            str = "**Password and Confirm Password are not same"
        }else if (!pw.matches(Regex(passwordPattern))) {
            str = "**Password must be at least 6 characters long, and contain one lowercase letter, one uppercase letter, one digit, and one special character"
            err = true}

        if (err) {
            binding.tverror.text=str
            fadeInView(binding.tverror)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.tverror)
            }, 1500)
        } else if(err2){
            binding.tverror.text=str
            fadeInView(binding.tverror)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.tverror)
            }, 1500)
        }else{
            val editor  = sharedPreferences.edit()
            editor.putString("password", pw)
            val intent = Intent(this,GenderActivity::class.java)
            editor.apply()
            editor.commit()


            startActivity(intent)

            this@PasswordActivity.overridePendingTransition(R.anim.animate_swipe_left_enter,R.anim.animate_swipe_left_exit)
        }
    }

    private fun createUser(pw:String) {
        val email = sharedPreferences.getString("email","abc@xyz")

        auth.createUserWithEmailAndPassword(email!!,pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Successfully Created User",Toast.LENGTH_SHORT)
                } else {


                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null && user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
                        linkEmailAndPassword(email, pw)
                    } else {
                        val errorMessage = task.exception?.message
                        Log.e("FirebaseAuth1", "Error: $errorMessage")
                        Toast.makeText(this, "Error Signing up", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
    private fun linkEmailAndPassword(email: String, password: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(email, password)
        user?.linkWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Successfully linked email and password", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = task.exception?.message
                    Log.d("FirebaseAuth2", "Error: $errorMessage")
                    Toast.makeText(this, "Error Signing UP", Toast.LENGTH_LONG).show()
                }
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

    // Function to fade out the view
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