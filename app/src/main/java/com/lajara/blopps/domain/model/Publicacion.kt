package com.lajara.blopps.domain.model

data class Movie(
    val id: String,
    val titulo: String,
    val sinopsis: String,
    val director: String,
    val calificacion: Double,
    val posterUrl: String?,
    val anio: Int?,
    val comunidad: Comunidad,
    val esFavorita: Boolean = false
)