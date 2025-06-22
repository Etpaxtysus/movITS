package com.example.fpmobileprogramming

import MovieApiService
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private val movieApiService = MovieApiService()

    var popularMovies by mutableStateOf<List<Movie>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchPopularMovies()
    }

    fun fetchPopularMovies() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                popularMovies = movieApiService.getPopularMovies()
                Log.d("MovieViewModel", "Popular movies updated: ${popularMovies.size}")
            } catch (e: Exception) {
                errorMessage = "Failed to load movies: ${e.message}"
                popularMovies = emptyList()
                Log.e("MovieViewModel", "Error fetching movies: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}