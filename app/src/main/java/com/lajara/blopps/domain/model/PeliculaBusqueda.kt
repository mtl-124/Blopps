package com.lajara.blopps.domain.model

data class PeliculaBusqueda(
    val id: Int,
    val titulo: String,
    val posterUrl: String?,
    val anio: Int?,
    val calificacion: Double,
    val sinopsis: String
)
