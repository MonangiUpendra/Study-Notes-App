package com.example.studynotesapp.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.studynotesapp.MainActivity
import com.example.studynotesapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra("noteId")
        val user = FirebaseAuth.getInstance().currentUser

        Log.d("REMINDER", "🔔 ReminderReceiver triggered")
        Log.d("REMINDER", "Note ID: $noteId")
        Log.d("REMINDER", "User ID: ${user?.uid}")

        // Firestore Update
        if (noteId != null && user != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .collection("notes")
                .document(noteId)
                .update("hasReminder", false)
                .addOnSuccessListener {
                    Log.d("REMINDER", "✅ hasReminder=false updated in Firestore")
                }
                .addOnFailureListener {
                    Log.e("REMINDER", "❌ Failed to update hasReminder", it)
                }
        } else {
            Log.e("REMINDER", "Missing noteId or user is null!")
        }

        // Optional: Show notification
        val title = intent.getStringExtra("title") ?: "Reminder"
        val content = intent.getStringExtra("content") ?: "Don't forget your note!"
        val channelId = "reminder_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
