package com.example.studynotesapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studynotesapp.screens.*
import com.example.studynotesapp.ui.theme.StudyNotesAppTheme
import com.example.studynotesapp.ThemePreference
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val NOTIFICATION_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Load saved dark mode preference and apply it
        val isDarkMode = ThemePreference.isNightMode(this)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode)
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else
                androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )

        // ✅ Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }

        // ✅ Initialize Firebase
        val firebaseApp = FirebaseApp.initializeApp(this)
        if (firebaseApp != null) {
            Toast.makeText(this, "✅ Firebase Connected!", Toast.LENGTH_LONG).show()
            Log.d("FIREBASE", "Firebase initialized successfully")
        } else {
            Toast.makeText(this, "❌ Firebase Not Connected!", Toast.LENGTH_LONG).show()
            Log.e("FIREBASE", "Firebase initialization failed")
        }

        // ✅ Get FCM Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "🔥 FCM Token: $token")
                Toast.makeText(this, "FCM Token fetched. Check Logcat.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("FCM_TOKEN", "❌ Failed to get token", task.exception)
            }
        }

        // ✅ Compose UI Setup
        setContent {
            StudyNotesAppTheme(
                darkTheme = isDarkMode // Apply dark mode based on saved preference
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                    composable("home") { HomeScreen(navController) }
                    composable("addNote") { AddNoteScreen(navController) }
                    composable("notes") { NotesListScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                }
            }
        }
    }
}
