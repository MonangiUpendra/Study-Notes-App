package com.example.studynotesapp.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.TimePickerDialog
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studynotesapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun AddNoteScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    var reminderHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var reminderMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var reminderSet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Add New Note", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 10
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            TimePickerDialog(
                context,
                { _: TimePicker, hour: Int, minute: Int ->
                    reminderHour = hour
                    reminderMinute = minute
                    reminderSet = true
                    Toast.makeText(context, "Reminder set for $hour:$minute", Toast.LENGTH_SHORT).show()
                },
                reminderHour,
                reminderMinute,
                true
            ).show()
        }) {
            Text("Set Reminder Time")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (title.isBlank() || content.isBlank()) {
                    errorMessage = "Please enter both title and content"
                    return@Button
                }

                errorMessage = null
                isSaving = true

                val noteData = hashMapOf(
                    "title" to title,
                    "content" to content,
                    "timestamp" to com.google.firebase.Timestamp.now(),
                    "hasReminder" to reminderSet  // ✅ Save reminder flag
                )

                user?.uid?.let { uid ->
                    db.collection("users")
                        .document(uid)
                        .collection("notes")
                        .add(noteData)
                        .addOnSuccessListener { documentRef ->
                            val noteId = documentRef.id // ✅ Get the note's ID

                            // 🔒 Android 12+ exact alarm permission check
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    context.startActivity(intent)
                                }
                            }

                            // ✅ Schedule reminder if set
                            if (reminderSet) {
                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val intent = Intent(context, ReminderReceiver::class.java).apply {
                                    putExtra("title", "StudyNoters Reminder")
                                    putExtra("content", title)
                                    putExtra("noteId", noteId)  // ✅ Pass note ID
                                }

                                val pendingIntent = PendingIntent.getBroadcast(
                                    context,
                                    System.currentTimeMillis().toInt(),
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )

                                val alarmTime = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, reminderHour)
                                    set(Calendar.MINUTE, reminderMinute)
                                    set(Calendar.SECOND, 0)
                                    if (timeInMillis <= System.currentTimeMillis()) {
                                        add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                }

                                val alarmInfo = AlarmManager.AlarmClockInfo(alarmTime.timeInMillis, pendingIntent)
                                alarmManager.setAlarmClock(alarmInfo, pendingIntent)

                                Toast.makeText(context, "Reminder scheduled for ${alarmTime.time}", Toast.LENGTH_SHORT).show()
                            }

                            // Reset form
                            isSaving = false
                            title = ""
                            content = ""
                            navController.navigate("home") {
                                popUpTo("addNote") { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            isSaving = false
                            errorMessage = "Failed to save note: ${e.localizedMessage}"
                        }
                } ?: run {
                    isSaving = false
                    errorMessage = "User not logged in"
                }
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Text("Save Note")
            }
        }
    }
}
