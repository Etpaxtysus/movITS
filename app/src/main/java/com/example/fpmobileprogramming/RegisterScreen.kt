package com.example.fpmobileprogramming

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore // Import Firestore

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") } // New state for full name
    var dateOfBirth by remember { mutableStateOf("") } // New state for date of birth
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = Firebase.auth
    val db = Firebase.firestore // Initialize Firestore instance

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateOfBirth = sdf.format(selectedDate.time)
            errorMessage = null // Clear error when date is selected
        }, year, month, day
    )

    Box(modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = {Text("Email")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fullName, // New TextField for full name
                onValueChange = {
                    fullName = it
                    errorMessage = null
                },
                label = {Text("Full Name")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth TextField
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { /* Prevent direct editing */ },
                label = { Text("Date of Birth") },
                readOnly = true, // Make it read-only
                trailingIcon = { // Add calendar icon to open DatePicker
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() } // Make whole field clickable
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = {Text("Password")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = {Text("Confirm Password")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(onClick = {
                // Improved validation logic to include new fields
                if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || fullName.isBlank() || dateOfBirth.isBlank()) {
                    errorMessage = "All fields must be filled."
//                    Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                } else if (password != confirmPassword) {
                    errorMessage = "Passwords do not match."
//                    Toast.makeText(context, "Password and Confirm Password do not match.", Toast.LENGTH_SHORT).show()
                } else if (password.length < 6) { // Add password length validation
                    errorMessage = "Password must be at least 6 characters long."
//                    Toast.makeText(context, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                }
                else {
                    errorMessage = null // Clear error if inputs are valid
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.let {
                                    val userData = hashMapOf(
                                        "fullName" to fullName,
                                        "dateOfBirth" to dateOfBirth,
                                        "email" to email
                                    )

                                    db.collection("users") // Collection "users"
                                        .document(it.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Sign Up Successful! Please log in.", Toast.LENGTH_SHORT).show()
                                            navController.navigate("login") {
                                                popUpTo("register") {inclusive = true}
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                            user.delete()
                                        }
                                }
                            }
                            else {
                                // Display Firebase Auth error message if registration fails
                                Toast.makeText(context, task.exception?.message?:"Sign Up Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Sign Up")
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick =  {
                navController.navigate("login") {
                    popUpTo("register") {inclusive = true}
                }
            }) {
                Text("Already have an account? Log In")
            }
        }
    }
}