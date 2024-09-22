package com.moviematch.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.moviematch.R
import com.moviematch.SigninPage
import com.moviematch.databinding.FragmentDashboardBinding
import com.moviematch.utils.ProgressBar


class DashboardFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    lateinit var userSharedPreferences: SharedPreferences
lateinit var binding: FragmentDashboardBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ProgressBar.showProgressBar(requireContext())
        binding = FragmentDashboardBinding.inflate(inflater,container,false)
        return binding.root
       }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        sharedPreferences = requireContext().getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
        userSharedPreferences = requireContext().getSharedPreferences("USER",Context.MODE_PRIVATE)
        var curruser = mAuth.currentUser
        val username = userSharedPreferences.getString("username",null)
        if(username==null){
            Toast.makeText(requireContext(), "Please sign in first.", Toast.LENGTH_SHORT).show()
            val editor = userSharedPreferences.edit()
            curruser=null
            editor.clear()
            editor.apply()
            editor.commit()
            val intent = Intent(requireContext(), SigninPage::class.java)
            startActivity(intent)
        }else{

            fetchUserDetails(username)
        }


        binding.BtnLogout.setOnClickListener {
            signOutAndStartSignInActivity()
        }

    }

    private fun fetchUserDetails(username: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Users")
            .whereEqualTo("Username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val name = document.getString("Name") ?: "User"
                    val gender = document.getString("Gender") ?: "Gender"
                    val email = document.getString("Email") ?: "abc@xyz"
                    val pw = document.getString("Password") ?: "123456"

                    storeUserDetails(name, username, gender, email, pw)
                    
                    binding.tvUser.text = name
                    binding.tvEmail.text = email
                    binding.tvNum.text = gender
                   ProgressBar.dismissProgressBar()
                } else {
                    Toast.makeText(requireContext(), "User details not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching user details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeUserDetails(name: String, username: String, gender: String, email: String, pw: String) {
        val editor = userSharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("username", username)
        editor.putString("gender", gender)
        editor.putString("email", email)
        editor.putString("password", pw)
        editor.apply()
        editor.commit()
    }


    private fun signOutAndStartSignInActivity() {
        val editor = userSharedPreferences.edit()
        editor.clear()
        editor.apply()
        mAuth.signOut()
        mGoogleSignInClient.signOut()
            val intent = Intent(requireContext(),SigninPage::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        val fragmentManager = parentFragmentManager
        fragmentManager.popBackStack()
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.remove(this).commit()
        }

}
