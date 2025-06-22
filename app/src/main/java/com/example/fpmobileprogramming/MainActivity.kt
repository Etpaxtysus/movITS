package com.example.fpmobileprogramming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fpmobileprogramming.ui.theme.FPMobileProgrammingTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            FPMobileProgrammingTheme {
                AuthApp()
            }
        }
    }
}

@Composable
fun AuthApp(){
    val navController = rememberNavController()
    val currentUser = Firebase.auth.currentUser

    // Determine the start destination based on user login status
    val startDestination = if (currentUser != null) {
        "home" // User is logged in, go directly to home
    } else {
        "onboarding" // User is not logged in, go to onboarding
    }

    NavHost(navController, startDestination = startDestination) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController)}
        composable("register") { RegisterScreen(navController)}
        composable("home") { HomeScreen(navController)}
    }
}

