import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import android.util.Log
import com.example.fpmobileprogramming.Movie
import com.example.fpmobileprogramming.MovieResponse

class MovieApiService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("HttpClient", message)
                }
            }
            level = LogLevel.ALL // Sangat penting untuk melihat detail request/response
        }
    }

    private val BASE_URL = "https://api.themoviedb.org/3"
    private val API_KEY = "" // Credentials

    suspend fun getPopularMovies(): List<Movie> {
        return try {
            val url = "$BASE_URL/movie/popular?api_key=$API_KEY&language=en-US&page=1"
            Log.d("MovieApiService", "Requesting URL: $url") // Tambahkan log URL
            val response: MovieResponse = httpClient.get(url).body()
            Log.d("MovieApiService", "Received movies: ${response.results.size}") // Log jumlah film
            response.results
        } catch (e: Exception) {
            Log.e("MovieApiService", "Error fetching popular movies: ${e.message}", e) // Log error yang lebih detail
            emptyList()
        }
    }

    suspend fun getMovieDetail(movieId: Int): Movie? {
        return try {
            val url = "$BASE_URL/movie/$movieId?api_key=$API_KEY&language=en-US"
            Log.d("MovieApiService", "Requesting Movie Detail URL: $url")
            httpClient.get(url).body<Movie>() // Langsung deserialisasi ke objek Movie
        } catch (e: Exception) {
            Log.e("MovieApiService", "Error fetching movie detail for ID $movieId: ${e.message}", e)
            null // Kembalikan null jika ada error
        }
    }

    fun getFullPosterUrl(posterPath: String?): String? {
        return if (posterPath != null) {
            "https://image.tmdb.org/t/p/w500$posterPath"
        } else {
            null
        }
    }
}