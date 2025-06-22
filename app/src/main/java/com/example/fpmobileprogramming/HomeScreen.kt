package com.example.fpmobileprogramming

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val currentUser = Firebase.auth.currentUser // Get the currently logged-in user
    val context = LocalContext.current

    var selectedItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavItem("home_tab", Icons.Default.Home, "Home"),
        BottomNavItem("my_order_tab", Icons.Default.List, "My Order"),
        BottomNavItem("my_profile_tab", Icons.Default.AccountCircle, "My Profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == index,
                        onClick = {
                            if (index == 0) { // Home tab, always accessible
                                selectedItem = index
                            } else { // My Order or My Profile
                                if (currentUser != null) { // User is logged in
                                    selectedItem = index
                                } else { // User is not logged in, navigate to login
                                    Toast.makeText(context, "Please login to access this feature.", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login") {
                                        // Optional: pop up to the current destination to avoid back stack issues if needed
                                        popUpTo(navController.currentDestination?.route ?: "home") { inclusive = true }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        when (selectedItem) {
            0 -> HomeTabContent(modifier = Modifier.padding(paddingValues), navController = navController)
            1 -> {
                if (currentUser != null) {
                    MyOrderTabContent(modifier = Modifier.padding(paddingValues))
                } else {
                    // This case should ideally not be reached if the onClick logic works correctly
                    // But as a fallback, you can show a message or redirect.
                    // For now, let's keep it empty or redirect to login.
                    LoginRequiredScreen(navController = navController, modifier = Modifier.padding(paddingValues))
                }
            }
            2 -> {
                if (currentUser != null) {
                    MyProfileTabContent(navController = navController, userId = currentUser.uid, modifier = Modifier.padding(paddingValues))
                } else {
                    // This case should ideally not be reached if the onClick logic works correctly
                    // But as a fallback, you can show a message or redirect.
                    LoginRequiredScreen(navController = navController, modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

// Composable Home
@Composable
fun HomeTabContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val currentUser = Firebase.auth.currentUser
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to the Home Screen!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Discover amazing movies and shows here.")
            Spacer(modifier = Modifier.height(24.dp))
            if (currentUser == null) {
                Button(onClick = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Login Now")
                }
            }
        }
    }
}

// Composable Order
@Composable
fun MyOrderTabContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "My Orders",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "You don't have any orders yet.") // Placeholder
        }
    }
}

// Composable Profile
@Composable
fun MyProfileTabContent(navController: NavHostController, userId: String, modifier: Modifier = Modifier) {
    var fullName by remember { mutableStateOf<String?>(null) }
    val db = Firebase.firestore
    val context = LocalContext.current
    val webClientId = "" // Credentials

    LaunchedEffect(userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    fullName = document.getString("fullName")
                } else {
                    println("No such document for user UID: $userId")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting user document: $exception")
            }
    }

    Box(modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Hello${fullName?.let { ", $it" } ?: ""}!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "This is your profile page.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                signOut(context, webClientId) {
                    Toast.makeText(context, "Logout Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate("onboarding") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }) { Text("Logout") }
        }
    }
}

@Composable
fun LoginRequiredScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Please Login to Access This Feature",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }) {
                Text("Go to Login")
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