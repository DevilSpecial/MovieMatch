package com.moviematch.Rooms

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.moviematch.R
import com.moviematch.databinding.ActivityStartRoomBinding

class StartRoom : AppCompatActivity() {
    lateinit var binding: ActivityStartRoomBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      binding= ActivityStartRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnCreate.setOnClickListener {
            startActivity(Intent(this,HostActivity::class.java))
        }
        binding.btnJoin.setOnClickListener {
            startActivity(Intent(this,GuestActivity::class.java))
        }
    }
}