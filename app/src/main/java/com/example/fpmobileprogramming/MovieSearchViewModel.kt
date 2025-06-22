package com.example.fpmobileprogramming

import MovieApiService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MovieSearchViewModel : ViewModel() {
    private val movieApiService = MovieApiService()

    var searchResults by mutableStateOf<List<Movie>>(emptyList())
        private set

    var isLoadingSearch by mutableStateOf(false)
        private set

    var searchErrorMessage by mutableStateOf<String?>(null)
        private set

    private var searchJob: Job? = null

    fun searchMovies(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            searchResults = emptyList()
            searchErrorMessage = null
            isLoadingSearch = false
            return
        }

        isLoadingSearch = true
        searchErrorMessage = null
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            try {
                val results = movieApiService.searchMovies(query)
                searchResults = results
            } catch (e: Exception) {
                searchErrorMessage = "Failed to search movies: ${e.message}"
                searchResults = emptyList()
            } finally {
                isLoadingSearch = false
            }
        }
    }

    fun clearSearchResults() {
        searchJob?.cancel()
        searchResults = emptyList()
        searchErrorMessage = null
        isLoadingSearch = false
    }
}