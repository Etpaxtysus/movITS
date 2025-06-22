package com.example.fpmobileprogramming

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth // Import fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun OnboardingScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp), // Padding di sini akan berlaku untuk seluruh konten
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Konten utama (Logo, Judul, Tagline) mengambil sisa ruang vertikal
        Column(
            modifier = Modifier
                .weight(1f) // Membuat kolom ini mengambil semua ruang yang tersedia
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center, // Pusatkan konten ini secara vertikal di ruangnya
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Aplikasi
            Image(
                painter = painterResource(id = R.drawable.movits_logo),
                contentDescription = "movITS App Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Judul Aplikasi
            Text(
                text = "Welcome to movITS!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tagline atau Deskripsi Singkat
            Text(
                text = "Your one-stop destination for movie tickets.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        // Tombol "Get Started" diposisikan di bagian bawah
        Spacer(modifier = Modifier.height(48.dp)) // Jarak antara konten atas dan tombol
        Button(
            onClick = {
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth() // Membuat tombol mengisi lebar penuh
                .padding(horizontal = 32.dp) // Tambahkan padding horizontal untuk estetika
                .height(56.dp) // Sesuaikan tinggi tombol agar lebih proporsional
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium, ) // Opsional: sesuaikan gaya teks tombol
        }
        Spacer(modifier = Modifier.height(16.dp)) // Jarak dari tepi bawah layar
    }
}