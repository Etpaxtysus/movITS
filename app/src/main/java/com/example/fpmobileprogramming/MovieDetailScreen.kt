package com.example.fpmobileprogramming

import MovieApiService
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch // Tambahkan import ini
import android.util.Log // Tambahkan import ini untuk Log
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavHostController,
    movieId: Int
) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var isLoadingDetail by remember { mutableStateOf(true) }
    var errorDetailMessage by remember { mutableStateOf<String?>(null) }
    val movieApiService = remember { MovieApiService() }

    LaunchedEffect(movieId) {
        isLoadingDetail = true
        errorDetailMessage = null
        try {
            val fetchedMovie = movieApiService.getMovieDetail(movieId)
            if (fetchedMovie != null) {
                movie = fetchedMovie
            } else {
                errorDetailMessage = "Movie details not found."
            }
        } catch (e: Exception) {
            Log.e("MovieDetailScreen", "Error fetching movie details: ${e.message}", e)
            errorDetailMessage = "Failed to load movie details: ${e.message}"
        } finally {
            isLoadingDetail = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movie?.title ?: "Movie Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingDetail) {
                CircularProgressIndicator()
                Text("Loading movie details...")
            } else if (errorDetailMessage != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $errorDetailMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            } else if (movie == null) {
                Text("Movie details not found.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val imageUrl = movieApiService.getFullPosterUrl(movie?.posterPath)
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = movie?.title,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(300.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Poster Available")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = movie?.title ?: "N/A",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rating: ${String.format("%.1f", movie?.voteAverage)}/10 (${movie?.voteCount} votes)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genre: ${movie?.genres?.joinToString(", ") { it.name } ?: "N/A"}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Release Date: ${movie?.releaseDate ?: "N/A"}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Overview:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = movie?.overview ?: "No overview available.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Additional Information:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Menggunakan fungsi formatCurrency
                    Text(
                        text = "Budget: ${formatCurrency(movie?.budget)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Revenue: ${formatCurrency(movie?.revenue)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${movie?.status ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tagline: ${movie?.tagline ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Homepage: ${movie?.homepage ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Menggunakan fungsi getLanguageDisplayName
                    Text(
                        text = "Original Language: ${getLanguageDisplayName(movie?.originalLanguage)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Runtime: ${movie?.runtime ?: "N/A"} minutes",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spoken Languages: ${movie?.spokenLanguages?.joinToString(", ") { it.englishName } ?: "N/A"}", // Gunakan englishName
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Production Companies: ${movie?.productionCompanies?.joinToString(", ") { it.name } ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Production Countries: ${movie?.productionCountries?.joinToString(", ") { it.name } ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Belongs To Collection: ${movie?.belongsToCollection?.name ?: "N/A"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Pastikan fungsi-fungsi ini berada di luar Composable,
// biasanya di bagian paling bawah file atau di file utilitas terpisah
fun formatCurrency(amount: Long?): String {
    if (amount == null || amount <= 0) {
        return "N/A"
    }
    // Menggunakan Locale.US untuk format mata uang Dolar ($)
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

fun getLanguageDisplayName(isoCode: String?): String {
    if (isoCode == null || isoCode.isBlank()) {
        return "N/A"
    }
    // Menggunakan Locale untuk mendapatkan nama bahasa yang ditampilkan
    // Perhatikan bahwa Locale hanya dapat mengonversi kode bahasa ISO 639-1 ke nama yang ditampilkan.
    // Jika API memberikan kode seperti 'en-US', Anda mungkin perlu memisahkannya terlebih dahulu.
    val locale = Locale(isoCode)
    return locale.displayLanguage
}