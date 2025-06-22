package com.example.fpmobileprogramming

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class Movie(
    val id: Int,
    val adult: Boolean,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("genre_ids") val genreIds: List<Int>? = null,
    val genres: List<Genre>? = null,
    val overview: String,
    val popularity: Double,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("release_date") val releaseDate: String?,
    val title: String,
    val video: Boolean,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
     @SerialName("belongs_to_collection") val belongsToCollection: CollectionInfo? = null,
     val budget: Long? = null,
     val homepage: String? = null,
     @SerialName("imdb_id") val imdbId: String? = null,
     @SerialName("origin_country") val originCountry: List<String>? = null,
     @SerialName("original_language") val originalLanguage: String,
     @SerialName("production_companies") val productionCompanies: List<ProductionCompany>? = null,
     @SerialName("production_countries") val productionCountries: List<ProductionCountry>? = null,
     val revenue: Long? = null,
     val runtime: Int? = null,
     @SerialName("spoken_languages") val spokenLanguages: List<SpokenLanguage>? = null,
     val status: String? = null,
     val tagline: String? = null
)

@Serializable
data class CollectionInfo(
    val id: Int,
    val name: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?
)

@Serializable
data class ProductionCompany(
    val id: Int,
    @SerialName("logo_path") val logoPath: String?,
    val name: String,
    @SerialName("origin_country") val originCountry: String
)

@Serializable
data class ProductionCountry(
    @SerialName("iso_3166_1") val iso31661: String,
    val name: String
)

@Serializable
data class SpokenLanguage(
    @SerialName("english_name") val englishName: String,
    @SerialName("iso_639_1") val iso6391: String,
    val name: String
)
