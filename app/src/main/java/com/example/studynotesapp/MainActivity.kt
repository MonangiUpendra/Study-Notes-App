package com.example.studynotesapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studynotesapp.screens.LoginScreen
import com.example.studynotesapp.screens.RegisterScreen
import com.example.studynotesapp.screens.HomeScreen
import com.example.studynotesapp.screens.SplashScreen
import com.example.studynotesapp.screens.AddNoteScreen
import com.example.studynotesapp.screens.NotesListScreen

import com.example.studynotesapp.ui.theme.StudyNotesAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseApp = FirebaseApp.initializeApp(this)
        if (firebaseApp != null) {
            Toast.makeText(this, "✅ Firebase Connected!", Toast.LENGTH_LONG).show()
            Log.d("FIREBASE", "Firebase initialized successfully")
        } else {
            Toast.makeText(this, "❌ Firebase Not Connected!", Toast.LENGTH_LONG).show()
            Log.e("FIREBASE", "Firebase initialization failed")
        }

        setContent {
            StudyNotesAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        SplashScreen(navController)
                    }
                    composable("login") {
                        LoginScreen(navController = navController)
                    }
                    composable("register") {
                        RegisterScreen(navController = navController)
                    }
                    composable("home") {
                        HomeScreen(navController = navController)
                    }
                    composable("addNote") {
                        AddNoteScreen(navController)
                    }
                    composable("notes") {
                        NotesListScreen(navController)
                    }


                }
            }
        }
    }
}
