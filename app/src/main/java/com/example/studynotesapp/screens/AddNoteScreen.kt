package com.example.studynotesapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AddNoteScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

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

                // Save note asynchronously
                val noteData = hashMapOf(
                    "title" to title,
                    "content" to content,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )

                user?.uid?.let { uid ->
                    db.collection("users")
                        .document(uid)
                        .collection("notes")
                        .add(noteData)
                        .addOnSuccessListener {
                            isSaving = false
                            // Clear inputs
                            title = ""
                            content = ""
                            // Navigate back to home or notes list
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
