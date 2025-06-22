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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import android.util.Log
import androidx.compose.foundation.background
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
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
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
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingDetail) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("Loading movie details...", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onBackground)
                }
            } else if (errorDetailMessage != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error: $errorDetailMessage",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            } else if (movie == null) {
                Text("Movie details not found.", color = MaterialTheme.colorScheme.onBackground)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    val imageUrl = movieApiService.getFullPosterUrl(movie?.posterPath)
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = movie?.title,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(400.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Poster Available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = movie?.title ?: "N/A",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rating: ${String.format("%.1f", movie?.voteAverage)}/10 (${movie?.voteCount} votes)",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Genre: ${movie?.genres?.joinToString(", ") { it.name } ?: "N/A"}",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Release Date: ${movie?.releaseDate ?: "N/A"}",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Overview:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = movie?.overview ?: "No overview available.",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Additional Information:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            MovieDetailInfoRow(label = "Budget", value = formatCurrency(movie?.budget))
                            MovieDetailInfoRow(label = "Revenue", value = formatCurrency(movie?.revenue))
                            MovieDetailInfoRow(label = "Status", value = movie?.status ?: "N/A")
                            MovieDetailInfoRow(label = "Tagline", value = movie?.tagline ?: "N/A")
                            MovieDetailInfoRow(label = "Homepage", value = movie?.homepage ?: "N/A")
                            MovieDetailInfoRow(label = "Original Language", value = getLanguageDisplayName(movie?.originalLanguage))
                            MovieDetailInfoRow(label = "Runtime", value = "${movie?.runtime ?: "N/A"} minutes")
                            MovieDetailInfoRow(label = "Spoken Languages", value = movie?.spokenLanguages?.joinToString(", ") { it.englishName } ?: "N/A")
                            MovieDetailInfoRow(label = "Production Companies", value = movie?.productionCompanies?.joinToString(", ") { it.name } ?: "N/A")
                            MovieDetailInfoRow(label = "Production Countries", value = movie?.productionCountries?.joinToString(", ") { it.name } ?: "N/A")
                            MovieDetailInfoRow(label = "Belongs To Collection", value = movie?.belongsToCollection?.name ?: "N/A")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MovieDetailInfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatCurrency(amount: Long?): String {
    if (amount == null || amount <= 0) {
        return "N/A"
    }
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

fun getLanguageDisplayName(isoCode: String?): String {
    if (isoCode == null || isoCode.isBlank()) {
        return "N/A"
    }
    val locale = Locale(isoCode)
    return locale.displayLanguage
}