package com.moviematch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.moviematch.databinding.ActiviySigninBinding
import com.moviematch.utils.ProgressBar

class SigninPage : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    companion object {
        private const val RC_SIGN_IN = 9001
    }

    lateinit var auth : FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    lateinit var userSharedPreferences: SharedPreferences
    lateinit var binding: ActiviySigninBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActiviySigninBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        userSharedPreferences = getSharedPreferences("USER",Context.MODE_PRIVATE)
        setContentView(binding.root)
        auth= FirebaseAuth.getInstance()
        val curruser = auth.currentUser
        val presentUser = userSharedPreferences.getBoolean("currentUser",false)
        if(presentUser) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.googleSignin.setOnClickListener{
            signinwithgoogle()
        }

        binding.etvBtnSignin.setOnClickListener {
            startsigin()
        }
        binding.etvBtnSignup.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            this@SigninPage.overridePendingTransition(R.anim.animate_shrink_enter,R.anim.animate_shrink_exit)
        }
        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Show password
                binding.etPw2.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Hide password
                binding.etPw2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            binding.etPw2.setSelection(binding.etPw2.text?.length ?: 0)
    }}



    private fun startsigin() {
        var username= binding.etUserName.text.toString()
        var pw = binding.etPw2.text.toString()
        var err = false
        if (username.isEmpty() || pw.isEmpty()) {
            err = true
        }
        if (err) {
            fadeInView(binding.enterall)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.enterall)
            }, 1500)
        } else {
            ProgressBar.showProgressBar(this)
            checkCredentials(username, pw)

        }
    }
    private fun checkCredentials(username: String, password: String) {
        db.collection("Users")
            .whereEqualTo("Username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val storedPassword = document.getString("Password")
                    if (storedPassword == password) {
                        val editor  =userSharedPreferences.edit()
                        editor.putString("username",username)
                        editor.putBoolean("currentUser",true)
                        editor.apply()
                        editor.commit()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        this.overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit)
                    } else {
                        // Password mismatch
                        ProgressBar.dismissProgressBar()
                        Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    ProgressBar.dismissProgressBar()
                    Toast.makeText(this, "User does not exist. Please sign up.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                ProgressBar.dismissProgressBar()
                Log.w("TAG", "Error getting documents: ", exception)
                Toast.makeText(this, "Error checking credentials.", Toast.LENGTH_SHORT).show()
            }}
    private fun signinwithgoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                ProgressBar.showProgressBar(this)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                ProgressBar.dismissProgressBar()
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    ProgressBar.showProgressBar(this)
                    val email = auth.currentUser?.email
                    if (email != null) {
                        checkGoogleUserExists(email)
                    } else {
                        Toast.makeText(this, "Failed to get email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    ProgressBar.dismissProgressBar()
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun checkGoogleUserExists(email: String) {
        var editor = sharedPreferences.edit()
        db.collection("Users")
            .whereEqualTo("Email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    editor.putString("email", email)
                    editor.putString("name", auth.currentUser!!.displayName)
                    editor.apply()
                    editor.commit()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    this.overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit)
                } else {
                    editor.putString("email",email)
                    editor.putString("name", auth.currentUser!!.displayName)
                    editor.apply()
                    editor.commit()
                    Toast.makeText(this, "You are a new user. Kindly make your username", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, UserNameActivity ::class.java)
                    startActivity(intent)
                    finish()
                    this.overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit)
                }
            }
            .addOnFailureListener { exception ->
                ProgressBar.hideProgressBar()
                Log.w("TAG", "Error getting documents: ", exception)
                Toast.makeText(this, "Error checking user existence.", Toast.LENGTH_SHORT).show()
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
