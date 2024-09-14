package com.moviematch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RoomDeletionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val roomId = intent.getStringExtra("room_id")

        if (roomId != null) {
            // Delete the room from Firebase
            val database = Firebase.database.reference
            val roomRef = database.child("room").child(roomId)
            roomRef.removeValue()
                .addOnSuccessListener {

                    Log.d("RoomDeletionReceiver", "Room $roomId deleted successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("RoomDeletionReceiver", "Failed to delete room: ", e)
                }
        }
    }
}
