package com.moviematch


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import com.moviematch.databinding.SignUpActivityBinding

class SignUp : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    lateinit var auth : FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: SignUpActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        super.onCreate(savedInstanceState)
        auth= FirebaseAuth.getInstance()
        binding = SignUpActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etvBtnSignin.setOnClickListener {
            goback()
        }

        binding.btnGoogle.setOnClickListener{
            signinwithgoogle()
        }
        binding.btnOtp.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            var str =""
            var err = false
            if (name.isEmpty() || email.isEmpty()) {
                str = "**All Fields are required"
                err = true
            }else if (!email.matches(Regex(emailPattern))) {
                str = "**Please enter a valid email address"
                err = true
            }
            if (err) {
                binding.tverror.text=str
                fadeInView(binding.tverror)
                Handler(Looper.getMainLooper()).postDelayed({
                    fadeOutView(binding.tverror)
                }, 1500)
            }else{
                sendToMainPage(name,email)
            }
        }
    }
    private fun sendToMainPage(name:String,email: String) {
        val editor  = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("email", email)
        val intent = Intent(this, UserNameActivity::class.java)
        editor.apply()
        editor.commit()
        startActivity(intent)
        this@SignUp.overridePendingTransition(R.anim.animate_swipe_left_enter,R.anim.animate_swipe_left_exit)
    }
    private fun signinwithgoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SignUp.RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SignUp.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val name = auth.currentUser?.displayName
                    val email  = auth.currentUser?.email
                    val editor =  sharedPreferences.edit()
                    editor.putString("name", name)
                    editor.putString("email", email)
                    editor.apply()
                    startActivity(Intent(this, UserNameActivity::class.java))
                    this@SignUp.overridePendingTransition(R.anim.animate_swipe_left_enter,R.anim.animate_swipe_left_exit)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun goback() {
        val intent = Intent(this, SigninPage::class.java)
        startActivity(intent)
        this@SignUp.overridePendingTransition(R.anim.animate_shrink_enter,R.anim.animate_shrink_exit)
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