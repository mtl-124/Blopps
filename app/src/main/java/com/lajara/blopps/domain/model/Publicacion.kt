package com.lajara.blopps.domain.model

data class Publicacion(
    val id: Int,
    val autorId: Int,
    val titulo: String,
    val cuerpo: String,
    val comunidad: Comunidad,
    val esFavorita: Boolean,
    val pelicula: PeliculaInfo?
)

data class PeliculaInfo(
    val id: Int,
    val titulo: String,
    val posterUrl: String?,
    val calificacion: Double,
    val anio: Int?,
    val sinopsis: String,
    val director: String?
)