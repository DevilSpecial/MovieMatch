package com.moviematch.Rooms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moviematch.Matching
import com.moviematch.MatchingGenreActivity
import com.moviematch.R
import com.moviematch.RoomDeletionReceiver
import com.moviematch.databinding.ActivityHostBinding
import com.moviematch.databinding.ActivityTempBinding
import com.moviematch.tempActivity.RoomData
import kotlin.random.Random

class HostActivity : AppCompatActivity() {
    val room_id = generateRandomPassword()
    lateinit var userSharedPreferences: SharedPreferences
    lateinit var binding: ActivityHostBinding
    val database = Firebase.database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userSharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE)
        binding=ActivityHostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvCode.text = room_id

        val host_id  = userSharedPreferences.getString("username","User1")
        val myRef = database.getReference("room")
        val roomId = myRef.push().key
        val roomData = Room(
            hostId = host_id,
            password = room_id // 'password' matches the data class property name
        )
        if (roomId != null) {
            myRef.child(roomData.password!!).setValue(roomData)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, RoomDeletionReceiver::class.java)
            intent.putExtra("room_id", room_id)

            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val triggerTime = System.currentTimeMillis() + 20 * 60 * 1000 // 20 minutes in milliseconds
//            val triggerTime = System.currentTimeMillis() + 10000  // 10sec in milliseconds
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
binding.btnGenre.setOnClickListener {
    val intent = Intent(this,Matching::class.java)
    intent.putExtra("roomId",room_id)
    startActivity(intent)
//    intent.putExtra("roomId",room_id)
}

        binding.btnCopy.setOnClickListener {
            startCopy()
        }
        binding.btnShare.setOnClickListener {
            startShare()
        }
    }

    private fun startShare() {
        val code = binding.tvCode.text.toString()

        // Create an Intent to share the text
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"  // Set the intent type to plain text
            putExtra(Intent.EXTRA_TEXT, "Join my room with this code: $code. Let's start swiping together")  // Share the room code
        }

        // Launch the share chooser
        val chooser = Intent.createChooser(intent, "Share Room Code")
        startActivity(chooser)
    }

    private fun startCopy() {
        val code = binding.tvCode.text.toString() // Get the text from TextView

        // Get the clipboard system service
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a new ClipData with the text
        val clip = ClipData.newPlainText("Room Code", code)

        // Set the ClipData to the clipboard
        clipboard.setPrimaryClip(clip)

    }

    fun generateRandomPassword(): String {
        val passwordLength = 6
        val randomPassword = StringBuilder()

        repeat(passwordLength) {
            randomPassword.append(Random.nextInt(0, 10)) // Random number between 0 and 9
        }
        return randomPassword.toString()
    }
}