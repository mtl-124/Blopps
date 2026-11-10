package com.lajara.blopps.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieListResponseDto(
    val results: List<MovieDto> = emptyList()
)

@Serializable
data class MovieDto(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0
)

@Serializable
data class MovieDetailDto(
    val id: Int,
    val credits: CreditsDto? = null
)

@Serializable
data class CreditsDto(
    val crew: List<CrewDto> = emptyList()
)

@Serializable
data class CrewDto(
    val name: String = "",
    val job: String = ""
)