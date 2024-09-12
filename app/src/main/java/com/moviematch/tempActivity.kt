package com.moviematch

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.moviematch.databinding.ActivityTempBinding
import kotlin.random.Random


class tempActivity : AppCompatActivity() {
    val room_id = generateRandomPassword()
    lateinit var userSharedPreferences: SharedPreferences
    lateinit var binding: ActivityTempBinding
    val database = Firebase.database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userSharedPreferences = getSharedPreferences("USER", Context.MODE_PRIVATE)
        binding = ActivityTempBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username  = userSharedPreferences.getString("username","User1")
        val myRef = database.getReference("room")
        val roomId = myRef.push().key
        binding.temp.setOnClickListener {
             temp_guest()
        }
// Creating a data object to store in the room
        val roomData = RoomData(
            hostId = username,
            password = room_id // 'password' matches the data class property name
        )

// Storing the room data with the generated unique ID
        if (roomId != null) {
            myRef.child(roomData.password!!).setValue(roomData)
        }
    }
    fun generateRandomPassword(): String {
        val passwordLength = 6
        val randomPassword = StringBuilder()

        // Generate a random 6-digit number
        repeat(passwordLength) {
            randomPassword.append(Random.nextInt(0, 10)) // Random number between 0 and 9
        }

        return randomPassword.toString()
    }
    private fun temp_guest() {
        val passwordEntered = room_id

        val usenrame = "sanchit"
        val roomRef = database.getReference("room").child(passwordEntered)
        roomRef.get().addOnSuccessListener { dataSnapshot ->
            val roomData = dataSnapshot.getValue(RoomData::class.java)


            if (roomData != null) {
                roomRef.child("guest").setValue(usenrame)
                Toast.makeText(this, "Successfully joined the room!", Toast.LENGTH_SHORT).show()
            } else {
                // Room with that password doesn't exist
                Toast.makeText(this, "Invalid password or room not found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Room not found or other error.", Toast.LENGTH_SHORT).show()
        }
    }

    data class RoomData(
        val hostId: String?=null,
        val password: String?=null,
        val guestId:String?=null
    ){
        constructor(hostId: String?, password: String) : this(hostId, password, null)

    }
}