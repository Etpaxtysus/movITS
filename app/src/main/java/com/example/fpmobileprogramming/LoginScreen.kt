package com.example.fpmobileprogramming

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuthUserCollisionException

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var forgotPasswordDialogBox by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth

    val googleSignInOptions = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("") // Credentials
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Firebase.auth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val user = Firebase.auth.currentUser
                        val fullName = account.displayName
                        val userEmail = account.email

                        user?.let { firebaseUser ->
                            val userRef = db.collection("users").document(firebaseUser.uid)

                            userRef.get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        val updates = hashMapOf<String, Any>(
                                            "fullName" to (fullName ?: ""),
                                            "email" to (userEmail ?: "")
                                        )
                                        userRef.update(updates)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Google Sign-In Successful (Data Updated)", Toast.LENGTH_SHORT).show()
                                                navController.navigate("home") {
                                                    popUpTo("login") {inclusive = true}
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Failed to update user data: ${e.message}", Toast.LENGTH_LONG).show()
                                                navController.navigate("home") {
                                                    popUpTo("login") {inclusive = true}
                                                }
                                            }
                                    } else {

                                        val userData = hashMapOf(
                                            "fullName" to (fullName ?: ""),
                                            "email" to (userEmail ?: ""),
                                            "dateOfBirth" to ""
                                        )
                                        userRef.set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Google Sign-In Successful (New User)", Toast.LENGTH_SHORT).show()
                                                navController.navigate("home") {
                                                    popUpTo("login") {inclusive = true}
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Failed to save new user data: ${e.message}", Toast.LENGTH_LONG).show()
                                                firebaseUser.delete()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error checking user data: ${e.message}", Toast.LENGTH_LONG).show()
                                    navController.navigate("home") {
                                        popUpTo("login") {inclusive = true}
                                    }
                                }
                        }
                    }
                    else {
                        Toast.makeText(context, "Google Sign-In Failed: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        catch (e: Exception) {
            Toast.makeText(context,"Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // State for validation error message
    var errorMessage by remember { mutableStateOf<String?>(null) } // Nullable String

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null // Clear error message when user starts typing
                },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null // Clear error message when user starts typing
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    forgotPasswordDialogBox = true
                }) {
                    Text("Forgot Password?")
                }
            }

            // Display the error message if it exists
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() && password.isBlank()) {
                        errorMessage = "Email and password cannot be empty."
//                        Toast.makeText(context, "Please enter your email and password.", Toast.LENGTH_SHORT).show()
                    } else if (email.isBlank()) {
                        errorMessage = "Email cannot be empty."
//                        Toast.makeText(context, "Please enter your email.", Toast.LENGTH_SHORT).show()
                    } else if (password.isBlank()) {
                        errorMessage = "Password cannot be empty."
//                        Toast.makeText(context, "Please enter your password.", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMessage = null
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Login Successful!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Login Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))

            AndroidView(modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
                factory = { context ->
                    SignInButton(context).apply {
                        setSize(SignInButton.SIZE_WIDE)
                        setOnClickListener {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }
                    }
                })

            if (forgotPasswordDialogBox) {
                var resetEmail by remember { mutableStateOf("") }
                val contexts = LocalContext.current

                AlertDialog(
                    title = { Text("Forgot Password")},
                    text = {
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {resetEmail = it},
                            label = { Text("Email")},
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (resetEmail.isNotBlank()) {
                                Firebase.auth.sendPasswordResetEmail(resetEmail)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(contexts, "Check your email to reset password", Toast.LENGTH_SHORT).show()
                                            forgotPasswordDialogBox = false
                                        }
                                        else {
                                            Toast.makeText(contexts, "Registered email not found", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                            else {
                                Toast.makeText(contexts, "Please enter your registered email", Toast.LENGTH_SHORT).show()
                            }
                        }) { Text("Submit")}
                    }, dismissButton = {
                        TextButton(onClick = {forgotPasswordDialogBox = false}) {
                            Text("Cancel")
                        }
                    }, onDismissRequest = {forgotPasswordDialogBox = false}
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("register") {
                    popUpTo("login") { inclusive = true }
                }
            }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}