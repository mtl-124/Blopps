package com.lajara.blopps.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "publicaciones")
data class PublicacionEntity(
    @PrimaryKey val id: Int,
    val autorId: Int,
    val titulo: String,
    val cuerpo: String,
    val comunidad: String,
    val peliculaId: Int? = null,
    val peliculaTitulo: String? = null,
    val posterPath: String? = null,
    val sinopsis: String? = null,
    val calificacion: Double? = null,
    val anio: Int? = null,
    val director: String? = null
)