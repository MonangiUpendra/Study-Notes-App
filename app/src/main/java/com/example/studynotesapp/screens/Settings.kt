package com.example.studynotesapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studynotesapp.ThemePreference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var displayName by remember { mutableStateOf(TextFieldValue("")) }
    var isDarkMode by remember { mutableStateOf(ThemePreference.isNightMode(context)) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user?.uid) {
        if (displayName.text.isEmpty()) {
            user?.uid?.let { uid ->
                db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                    val name = doc.getString("displayName")
                    if (name != null) {
                        displayName = TextFieldValue(name)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("⚙️ Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Logged in as: ${user?.email ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val name = displayName.text.trim()
                    if (name.isNotEmpty() && user != null) {
                        val data = mapOf("displayName" to name)
                        db.collection("users")
                            .document(user.uid)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Display name updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Display Name")
            }

            Divider()

            // 🌙 Dark mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Dark Mode")
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        isDarkMode = it
                        scope.launch {
                            ThemePreference.setNightMode(context, it)
                            Toast.makeText(
                                context,
                                "Restart app to apply theme",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            // 🔒 Reset password with confirmation
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Password Reset Link")
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("Send Password Reset Email?") },
                    text = { Text("Do you want to send a password reset link to ${user?.email}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showResetDialog = false
                            user?.email?.let { email ->
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Reset link sent", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}
