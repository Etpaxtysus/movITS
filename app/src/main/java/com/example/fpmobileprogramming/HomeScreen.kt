package com.example.fpmobileprogramming

import MovieApiService
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

// Data class untuk item navigasi
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun HomeScreen(navController: NavHostController) { // navController sudah ada di sini
    val currentUser = Firebase.auth.currentUser
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
                            if (index == 0) {
                                selectedItem = index
                            } else {
                                if (currentUser != null) {
                                    selectedItem = index
                                } else {
                                    Toast.makeText(context, "Please login to access this feature.", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login")
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedItem) {
            0 -> MoviesListScreen(navController = navController, modifier = Modifier.padding(paddingValues)) // <--- Teruskan navController di sini
            1 -> {
                if (currentUser != null) {
                    MyOrderTabContent(modifier = Modifier.padding(paddingValues))
                } else {
                    LoginRequiredScreen(navController = navController, modifier = Modifier.padding(paddingValues))
                }
            }
            2 -> {
                if (currentUser != null) {
                    MyProfileTabContent(navController = navController, userId = currentUser.uid, modifier = Modifier.padding(paddingValues))
                } else {
                    LoginRequiredScreen(navController = navController, modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

// Composable untuk menampilkan daftar film
// Di dalam MoviesListScreen
@Composable
fun MoviesListScreen(
    navController: NavHostController, // <-- Tambahkan parameter ini
    modifier: Modifier = Modifier,
    movieViewModel: MovieViewModel = viewModel()
) {
    val movies = movieViewModel.popularMovies
    val isLoading = movieViewModel.isLoading
    val errorMessage = movieViewModel.errorMessage
    val movieApiService = remember { MovieApiService() }
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Popular Movies",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                CircularProgressIndicator()
                Text("Loading movies...")
            } else if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(onClick = { movieViewModel.fetchPopularMovies() }) {
                    Text("Retry")
                }
            } else if (movies.isEmpty()) {
                Text("No movies found.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(movies) { movie ->
                        MovieItem(movie = movie, movieApiService = movieApiService) { clickedMovie ->
                            // NAVIGASI KE DETAIL FILM DI SINI
                            navController.navigate("movieDetail/${clickedMovie.id}") // <-- Navigasi ke MovieDetailScreen
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieItem(movie: Movie, movieApiService: MovieApiService, onClick: (Movie) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(movie) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = movieApiService.getFullPosterUrl(movie.posterPath)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .width(90.dp)
                        .height(130.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Poster")
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rating: ${String.format("%.1f", movie.voteAverage)}/10",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = movie.releaseDate ?: "N/A",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3 // Batasi jumlah baris untuk overview
                )
            }
        }
    }
}


// Composable untuk konten tab My Order
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

// Composable untuk konten tab My Profile
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
                    // Setelah logout, navigasi kembali ke layar onboarding
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