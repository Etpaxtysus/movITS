package com.example.fpmobileprogramming

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@Composable
fun HomeScreen(navController: NavHostController) {
    val currentUser = Firebase.auth.currentUser // Get the currently logged-in user
    val context = LocalContext.current

    if (currentUser == null) {
        // User is not logged in, show unauthenticated home screen
        UnauthenticatedHomeScreen(navController = navController)
    } else {
        // User is logged in, show authenticated home screen
        AuthenticatedHomeScreen(navController = navController, currentUser.uid)
    }
}

@Composable
fun AuthenticatedHomeScreen(navController: NavHostController, userId: String) {
    var fullName by remember { mutableStateOf<String?>(null) } // State to hold the full name
    val db = Firebase.firestore // Get Firestore instance
    val context = LocalContext.current
    val webClientId = "" // Credentials

    LaunchedEffect(userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    fullName = document.getString("fullName") // Retrieve full name from Firestore
                } else {
                    println("No such document for user UID: $userId")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting user document: $exception")
            }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center) {

        Column (horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to the Home Screen${fullName?.let { ", $it" } ?: ""}!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                signOut(context, webClientId) {
                    Toast.makeText(context, "Logout Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("home") {inclusive = true}
                    }
                }
            }) { Text("Logout") }
        }
    }
}

@Composable
fun UnauthenticatedHomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to My App!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true } // Pop up to home so back button goes to home
                }
            }) {
                Text("Login Now")
            }
        }
    }
}

fun signOut(context: Context, webClientId: String, onComplete: () -> Unit) {
    Firebase.auth.signOut()

    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)
    googleSignInClient.signOut().addOnCompleteListener {
        onComplete()
    }
}