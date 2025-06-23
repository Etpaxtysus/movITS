package com.example.fpmobileprogramming

import MovieApiService
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.grid.items
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


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
    val isDarkTheme = isSystemInDarkTheme()

    var selectedItem by remember { mutableIntStateOf(0) }
    val bottomNavItems = listOf(
        BottomNavItem("home_tab", Icons.Default.Home, "Home"),
        BottomNavItem("search_movies_tab", Icons.Default.Search, "Search Movies"),
        // Tambahkan item "My Order" di sini
        BottomNavItem("my_order_tab", Icons.Default.ShoppingCart, "My Orders"),
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
                        val logoResource = if (isDarkTheme) R.drawable.movits_logo_dark else R.drawable.movits_logo
                        Image(
                            painter = painterResource(id = logoResource),
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
                                    2 -> "My Orders" // Tambahkan label untuk My Order
                                    3 -> "My Profile"
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == index,
                        onClick = {
                            if (index == 0) { // Home tab
                                selectedItem = index
                            } else { // Tabs requiring login
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
            2 -> { // Index for My Order tab
                if (currentUser != null) {
                    MyOrderTabContent(navController = navController, userId = currentUser.uid, modifier = Modifier.padding(paddingValues))
                } else {
                    LoginRequiredScreen(navController = navController, modifier = Modifier.padding(paddingValues))
                }
            }
            3 -> { // Index for My Profile tab
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 64.dp))
                Text("Loading movies...", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onBackground)
            } else if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 64.dp, bottom = 8.dp)
                )
                Button(onClick = { movieViewModel.fetchPopularMovies() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Retry")
                }
            } else if (movies.isEmpty()) {
                Text("No movies found.", modifier = Modifier.padding(top = 64.dp), color = MaterialTheme.colorScheme.onBackground)
            }
        }

        if (!isLoading && errorMessage == null && movies.isNotEmpty()) {
            val top4Movies = movies.take(4)
            val remainingMovies = movies.drop(4)

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        top4Movies.getOrNull(0)?.let { movie ->
                            MovieGridItem(
                                movie = movie,
                                movieApiService = movieApiService,
                                modifier = Modifier.weight(1f)
                            ) { clickedMovie ->
                                navController.navigate("movieDetail/${clickedMovie.id}")
                            }
                        }
                        top4Movies.getOrNull(1)?.let { movie ->
                            MovieGridItem(
                                movie = movie,
                                movieApiService = movieApiService,
                                modifier = Modifier.weight(1f)
                            ) { clickedMovie ->
                                navController.navigate("movieDetail/${clickedMovie.id}")
                            }
                        }
                    }
                    if (top4Movies.size > 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            top4Movies.getOrNull(2)?.let { movie ->
                                MovieGridItem(
                                    movie = movie,
                                    movieApiService = movieApiService,
                                    modifier = Modifier.weight(1f)
                                ) { clickedMovie ->
                                    navController.navigate("movieDetail/${clickedMovie.id}")
                                }
                            }
                            top4Movies.getOrNull(3)?.let { movie ->
                                MovieGridItem(
                                    movie = movie,
                                    movieApiService = movieApiService,
                                    modifier = Modifier.weight(1f)
                                ) { clickedMovie ->
                                    navController.navigate("movieDetail/${clickedMovie.id}")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (remainingMovies.isNotEmpty()) {
                item {
                    Text(
                        text = "More Movies",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp, start = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(remainingMovies) { movie ->
                            MovieHorizontalItem(movie = movie, movieApiService = movieApiService) { clickedMovie ->
                                navController.navigate("movieDetail/${clickedMovie.id}")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MovieGridItem(
    movie: Movie,
    movieApiService: MovieApiService,
    modifier: Modifier = Modifier,
    onClick: (Movie) -> Unit
) {
    Card(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .height(300.dp)
            .clickable { onClick(movie) },
        shape = RoundedCornerShape(12.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUrl = movieApiService.getFullPosterUrl(movie.posterPath)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Poster", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rating: ${String.format("%.1f", movie.voteAverage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MovieHorizontalItem(movie: Movie, movieApiService: MovieApiService, onClick: (Movie) -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(280.dp)
            .clickable { onClick(movie) },
        shape = RoundedCornerShape(12.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val imageUrl = movieApiService.getFullPosterUrl(movie.posterPath)
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Poster", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rating: ${String.format("%.1f", movie.voteAverage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                    contentDescription = "Search icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface
            )
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
            Text("Searching for movies...", color = MaterialTheme.colorScheme.onBackground)
        } else if (searchErrorMessage != null) {
            Text(
                text = "Error: $searchErrorMessage",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
            Text("No results found for \"$searchQuery\".", color = MaterialTheme.colorScheme.onBackground)
        } else if (searchResults.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(searchResults) { movie ->
                    MovieGridItem(movie = movie, movieApiService = movieApiService) { clickedMovie ->
                        navController.navigate("movieDetail/${clickedMovie.id}")
                    }
                }
            }
        } else {
            Text("Start typing to search for movies.", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun MyOrderTabContent(navController: NavHostController, userId: String, modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoadingOrders by remember { mutableStateOf(true) }
    var errorMessageOrders by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        isLoadingOrders = true
        errorMessageOrders = null
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("orderDate", Query.Direction.DESCENDING) // Urutkan berdasarkan tanggal terbaru
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MyOrderTabContent", "Listen failed.", e)
                    errorMessageOrders = "Failed to load orders: ${e.message}"
                    isLoadingOrders = false
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val fetchedOrders = snapshots.documents.mapNotNull { document ->
                        // Periksa apakah dokumen memiliki ID, jika tidak, Firestore akan menggenerasinya
                        // Kita asumsikan ID pesanan di Firestore adalah ID dokumennya
                        document.toObject(Order::class.java)?.copy(id = document.id)
                    }
                    orders = fetchedOrders
                    isLoadingOrders = false
                } else {
                    Log.d("MyOrderTabContent", "Current data: null")
                    isLoadingOrders = false
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoadingOrders) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text("Loading your orders...", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onBackground)
            }
        } else if (errorMessageOrders != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Error: $errorMessageOrders",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        } else if (orders.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "My Order Icon",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Orders Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You haven't purchased any tickets yet. Go to the movie details to buy one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Your Orders",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(orders) { order ->
                    OrderItemCard(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gambar Poster Film
            if (order.moviePosterUrl != null) {
                AsyncImage(
                    model = order.moviePosterUrl,
                    contentDescription = order.movieTitle,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Poster", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.movieTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tickets: ${order.ticketQuantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: ${formatCurrency(order.totalPrice.toLong())}", // Menggunakan formatCurrency
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                order.orderDate?.let { date ->
                    Text(
                        text = "Ordered On: ${dateFormat.format(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
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
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Please Login to Access This Feature",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
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