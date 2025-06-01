package com.example.studynotesapp.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(navController: NavController, noteId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var reminderHour by remember { mutableStateOf(0) }
    var reminderMinute by remember { mutableStateOf(0) }
    var reminderSet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Load existing note
    LaunchedEffect(noteId, user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).collection("notes").document(noteId)
                .get()
                .addOnSuccessListener { doc ->
                    title = doc.getString("title") ?: ""
                    content = doc.getString("content") ?: ""
                    reminderSet = doc.getBoolean("hasReminder") ?: false
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("✏️ Edit Note") }) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )

                Button(onClick = {
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            reminderHour = hour
                            reminderMinute = minute
                            reminderSet = true
                            Toast.makeText(context, "Reminder updated to $hour:$minute", Toast.LENGTH_SHORT).show()
                        },
                        reminderHour,
                        reminderMinute,
                        true
                    ).show()
                }) {
                    Text("⏰ Set Reminder")
                }

                Button(
                    onClick = {
                        if (title.isBlank() || content.isBlank()) {
                            Toast.makeText(context, "Please enter both title and content", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val updatedNote = mapOf(
                            "title" to title,
                            "content" to content,
                            "hasReminder" to reminderSet,
                            "timestamp" to Timestamp.now()
                        )

                        user?.uid?.let { uid ->
                            db.collection("users").document(uid)
                                .collection("notes").document(noteId)
                                .update(updatedNote)
                                .addOnSuccessListener {
                                    if (reminderSet) {
                                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                            !alarmManager.canScheduleExactAlarms()
                                        ) {
                                            // Ask user to enable exact alarm permission
                                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            context.startActivity(intent)
                                            Toast.makeText(
                                                context,
                                                "Please enable exact alarms in settings",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnSuccessListener
                                        }

                                        val alarmIntent = Intent(context, ReminderReceiver::class.java).apply {
                                            putExtra("title", "StudyNoters Reminder")
                                            putExtra("content", title)
                                            putExtra("noteId", noteId)
                                        }

                                        val pendingIntent = PendingIntent.getBroadcast(
                                            context,
                                            noteId.hashCode(),
                                            alarmIntent,
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

                                        Toast.makeText(context, "Reminder rescheduled", Toast.LENGTH_SHORT).show()
                                    }

                                    Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💾 Save Changes")
                }
            }
        }
    }
}
