package com.example.fpmobileprogramming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        "home" // User is logged in, go directly to home (which now has bottom nav)
    } else {
        "onboarding" // User is not logged in, go to onboarding
    }

    NavHost(navController, startDestination = startDestination) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController)}
        composable("register") { RegisterScreen(navController)}
        composable("home") { HomeScreen(navController)}
        // Rute untuk detail film
        composable(
            route = "movieDetail/{movieId}", // Definisikan rute dengan placeholder argumen
            arguments = listOf(navArgument("movieId") { type = NavType.IntType }) // Tentukan tipe argumen
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId")
            if (movieId != null) {
                MovieDetailScreen(navController = navController, movieId = movieId)
            } else {
                // Tangani kasus di mana movieId null (misalnya, navigasi ke layar error atau pop back)
                Text("Error: Movie ID not provided")
            }
        }
    }
}