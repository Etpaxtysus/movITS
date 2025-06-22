package com.example.fpmobileprogramming

import MovieApiService
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

import android.app.DatePickerDialog
import android.widget.DatePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val currentUser = Firebase.auth.currentUser
    val context = LocalContext.current

    var selectedItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavItem("home_tab", Icons.Default.Home, "Home"),
        BottomNavItem("search_movies_tab", Icons.Default.Search, "Search Movies"),
        BottomNavItem("my_profile_tab", Icons.Default.AccountCircle, "My Profile")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.movits_logo),
                            contentDescription = "movITS Logo",
                            modifier = Modifier
                                .size(45.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (selectedItem) {
                                    0 -> "Popular Movies"
                                    1 -> "Search Movies"
                                    2 -> "My Profile"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                // elevation = 8.dp
            ) {
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
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedItem) {
            0 -> MoviesListScreen(navController = navController, modifier = Modifier.padding(paddingValues))
            1 -> {
                if (currentUser != null) {
                    SearchMoviesTabContent(navController = navController, modifier = Modifier.padding(paddingValues))
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

@Composable
fun MoviesListScreen(
    navController: NavHostController,
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
                            navController.navigate("movieDetail/${clickedMovie.id}")
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
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
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
fun SearchMoviesTabContent(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    movieSearchViewModel: MovieSearchViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults = movieSearchViewModel.searchResults
    val isLoadingSearch = movieSearchViewModel.isLoadingSearch
    val searchErrorMessage = movieSearchViewModel.searchErrorMessage
    val movieApiService = remember { MovieApiService() }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            movieSearchViewModel.clearSearchResults()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                movieSearchViewModel.searchMovies(it)
            },
            label = { Text("Search movies by title...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    movieSearchViewModel.searchMovies(searchQuery)
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    movieSearchViewModel.searchMovies(searchQuery)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoadingSearch) {
            CircularProgressIndicator()
            Text("Searching for movies...")
        } else if (searchErrorMessage != null) {
            Text(
                text = "Error: $searchErrorMessage",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            Text("No results found for \"$searchQuery\".")
        } else if (searchResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { movie ->
                    MovieItem(movie = movie, movieApiService = movieApiService) { clickedMovie ->
                        navController.navigate("movieDetail/${clickedMovie.id}")
                    }
                }
            }
        } else {
            Text("Start typing to search for movies.")
        }
    }
}

@Composable
fun MyProfileTabContent(navController: NavHostController, userId: String, modifier: Modifier = Modifier) {
    var fullName by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var dateOfBirth by remember { mutableStateOf("") }
    val db = Firebase.firestore
    val context = LocalContext.current
    val webClientId = "" // Credentials

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
            val newDate = sdf.format(selectedDate.time)

            if (newDate != dateOfBirth) {
                val updates = hashMapOf<String, Any>(
                    "dateOfBirth" to newDate
                )
                db.collection("users").document(userId).update(updates)
                    .addOnSuccessListener {
                        dateOfBirth = newDate
                        Toast.makeText(context, "Date of birth updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update date of birth: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }, year, month, day
    )

    LaunchedEffect(userId) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    fullName = document.getString("fullName")
                    email = document.getString("email")
                    dateOfBirth = document.getString("dateOfBirth") ?: ""
                } else {
                    println("No such document for user UID: $userId")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting user document: $exception")
            }
    }

    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "Full Name",
                        value = fullName ?: "N/A"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = email ?: "N/A"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileInfoRow(
                        icon = Icons.Default.DateRange,
                        label = "Date of Birth",
                        value = dateOfBirth.ifBlank { "N/A" },
                        onClick = { datePickerDialog.show() },
                        actionIcon = Icons.Default.Edit
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    signOut(context, webClientId) {
                        Toast.makeText(context, "Logout Successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("onboarding") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        if (actionIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = actionIcon,
                contentDescription = "Edit $label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        } else if (onClick != null) {
            Spacer(modifier = Modifier.weight(1f))
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
            },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
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