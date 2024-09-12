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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.moviematch.databinding.ActivityPhoneNumberBinding
import com.moviematch.utils.ProgressBar
import java.util.concurrent.TimeUnit

class PhoneNumberActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var sharedPreferences: SharedPreferences
    val auth = Firebase.auth
    lateinit var OTP:String
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var binding : ActivityPhoneNumberBinding
    lateinit var resendtoken : PhoneAuthProvider.ForceResendingToken
    override fun onCreate(savedInstanceState: Bundle?) {
        OTP=""
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val name = sharedPreferences.getString("name","user")

        binding.tvSignup.text = "Hello $name,"
        mGoogleSignInClient = GoogleSignIn.getClient(this@PhoneNumberActivity, gso)
        binding.btnOtp.setOnClickListener {verify()}
        binding.btnCheck.setOnClickListener {verifyOTP()}
        binding.tvResend.setOnClickListener {resendVerificationCode()}
    }

    private fun verifyOTP() {
        var typedOtp = binding.etOtp.text.toString()
        if(typedOtp.length<6){
            Toast.makeText(this@PhoneNumberActivity,"Enter Valid OTP",Toast.LENGTH_SHORT).show()
        }else{
            val credential :PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    OTP,typedOtp
            )
        signInWithPhoneAuthCredential(credential)
        }
    }
    private fun resendVerificationCode(){
        binding.etOtp.text.clear();
        var number =  binding.etNum.text.toString()
        if(number.length<10){
            binding.tverror.text="Input Valid Number"
            fadeInView(binding.tverror)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.tverror)
            }, 1500)
        }else{
            number = "+91"+ number
            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setForceResendingToken(resendtoken)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

        }
    }
    private fun verify() {
       var number =  binding.etNum.text.toString()
        if(number.length<10){
            binding.tverror.text="Input Valid Number"
            fadeInView(binding.tverror)
            Handler(Looper.getMainLooper()).postDelayed({
                fadeOutView(binding.tverror)
            }, 1500)
        }else{
            ProgressBar.showProgressBar(this@PhoneNumberActivity)
            number = "+91"+ number

            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(number) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

        }
    }
   var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
       override fun onCodeSent(
           verificationId: String,
           token: PhoneAuthProvider.ForceResendingToken,
       ) {
           Handler(Looper.myLooper()!!).postDelayed(Runnable {
               binding.tvResend.visibility=View.VISIBLE
               binding.tvResend.isEnabled=true
           },60000)

           fadeOutView(binding.cardView)
           fadeOutView(binding.tverror)
           fadeOutView(binding.btnOtp)
           binding.tvText.text="Enter OTP send on number +91 "+ binding.etNum.text.toString()
           binding.tvOtpNum.text = "OTP sent on +91 " + binding.etNum.text.toString()

           fadeInView(binding.layoutOTP)
          ProgressBar.hideProgressBar()
           binding.etNum.clearFocus()

           binding.etOtp.requestFocus()

           OTP = verificationId
           resendtoken=token
       }
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@PhoneNumberActivity,"Invalid Credentials",Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(this@PhoneNumberActivity,"Request Quota finsished for today. Come back tomorrow :)",Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                Toast.makeText(this@PhoneNumberActivity,"Failed the Captcha Verification ",Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this@PhoneNumberActivity,PhoneNumberActivity::class.java)
            startActivity(intent)

        }}
       private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
           mAuth.signInWithCredential(credential)
               .addOnCompleteListener(this@PhoneNumberActivity) { task ->
                   if (task.isSuccessful) {
                       val editor  = sharedPreferences.edit()
                       val number = binding.etNum.text.toString()
                       editor.putString("number", number)

                       editor.apply()
                       editor.commit()
                       Toast.makeText(this@PhoneNumberActivity,"Authentication Successful",Toast.LENGTH_SHORT).show()
                       val intent = Intent(this@PhoneNumberActivity,PasswordActivity::class.java)

                       startActivity(intent)
                       this@PhoneNumberActivity.overridePendingTransition(R.anim.animate_swipe_left_enter,R.anim.animate_swipe_left_exit)
                       val user = task.result?.user
                   } else {
                       Log.w(TAG, "signInWithCredential:failure", task.exception)
                       if (task.exception is FirebaseAuthInvalidCredentialsException) {
                           Toast.makeText(this@PhoneNumberActivity,"Authentication Failed. Entered wrong OTP",Toast.LENGTH_LONG).show()
                       }

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