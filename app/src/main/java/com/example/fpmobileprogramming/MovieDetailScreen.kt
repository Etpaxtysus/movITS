package com.example.fpmobileprogramming

import MovieApiService
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog // Import AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField // Import OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Import TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf // Import mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.Scaffold
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random // Import Random
import androidx.compose.material3.TextFieldDefaults // Import TextFieldDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavHostController,
    movieId: Int
) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var isLoadingDetail by remember { mutableStateOf(true) }
    var errorDetailMessage by remember { mutableStateOf<String?>(null) }
    var showBuyTicketDialog by remember { mutableStateOf(false) } // State untuk visibilitas dialog

    val movieApiService = remember { MovieApiService() }
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser // Mendapatkan currentUser
    val db = Firebase.firestore // Mendapatkan instance Firestore

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
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = movie?.title ?: "Movie Details",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                },
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

                    Button(
                        onClick = {
                            if (currentUser != null && movie != null) {
                                showBuyTicketDialog = true // Tampilkan dialog
                            } else {
                                Toast.makeText(context, "Please log in to purchase tickets.", Toast.LENGTH_SHORT).show()
                                navController.navigate("login")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // Ikon Info, bisa diganti ikon tiket jika ada
                            contentDescription = "Buy Ticket",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buy Ticket Now", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }

    // Dialog Pembelian Tiket
    if (showBuyTicketDialog && movie != null) {
        BuyTicketDialog(
            movie = movie!!,
            onDismiss = { showBuyTicketDialog = false },
            onConfirmPurchase = { selectedQuantity, totalPrice ->
                val newOrder = Order(
                    userId = currentUser!!.uid, // Pastikan currentUser tidak null sebelum digunakan
                    movieId = movie!!.id,
                    movieTitle = movie!!.title,
                    moviePosterUrl = movieApiService.getFullPosterUrl(movie!!.posterPath),
                    ticketQuantity = selectedQuantity,
                    totalPrice = totalPrice
                )

                db.collection("orders")
                    .add(newOrder)
                    .addOnSuccessListener { documentReference ->
                        // Update ID pesanan dengan ID dokumen Firestore
                        db.collection("orders").document(documentReference.id)
                            .update("id", documentReference.id)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Ticket purchased successfully!", Toast.LENGTH_SHORT).show()
                                showBuyTicketDialog = false // Tutup dialog
                                navController.navigate("home") { // Kembali ke home (dan otomatis tab My Order akan diperbarui)
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to update order ID: ${e.message}", Toast.LENGTH_LONG).show()
                                showBuyTicketDialog = false
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to purchase ticket: ${e.message}", Toast.LENGTH_LONG).show()
                        showBuyTicketDialog = false
                    }
            },
            movieApiService = movieApiService // Meneruskan movieApiService untuk poster di dialog
        )
    }
}

// Composable Pembantu untuk Baris Info Detail
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

// Fungsi Pembantu untuk Format Mata Uang
fun formatCurrency(amount: Long?): String {
    if (amount == null || amount <= 0) {
        return "N/A"
    }
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

// Fungsi Pembantu untuk Nama Bahasa
fun getLanguageDisplayName(isoCode: String?): String {
    if (isoCode == null || isoCode.isBlank()) {
        return "N/A"
    }
    val locale = Locale(isoCode)
    return locale.displayLanguage
}

// Composable untuk Dialog Pembelian Tiket
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyTicketDialog(
    movie: Movie,
    movieApiService: MovieApiService,
    onDismiss: () -> Unit,
    onConfirmPurchase: (Int, Double) -> Unit
) {
    var ticketQuantityString by remember { mutableStateOf("1") } // <-- Ubah menjadi String
    val ticketPricePerUnit = remember { Random.nextDouble(4.0, 6.0) }

    // Konversi string ke Int untuk perhitungan, dengan default 1 jika tidak valid
    val currentTicketQuantity = ticketQuantityString.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val totalPrice = currentTicketQuantity * ticketPricePerUnit

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Buy Tickets for ${movie.title}",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val imageUrl = movieApiService.getFullPosterUrl(movie.posterPath)
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = movie.title,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Perbaiki format harga tiket per unit: gunakan toFloat() jika Random.nextDouble
                        Text(
                            text = "Price per ticket: ${formatCurrency(ticketPricePerUnit.toLong())}", // Format sebagai Long
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = ticketQuantityString, // <-- Bind ke String
                    onValueChange = { newValue ->
                        // Hanya update state string jika input adalah digit atau kosong
                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                            ticketQuantityString = newValue
                        }
                    },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Total Price: ${formatCurrency(totalPrice.toLong())}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                // Pastikan kuantitas minimal 1 saat konfirmasi
                onClick = { onConfirmPurchase(currentTicketQuantity, totalPrice) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm Purchase", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}