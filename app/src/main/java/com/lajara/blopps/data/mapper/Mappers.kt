package com.lajara.blopps.data.mapper

import com.lajara.blopps.data.local.PublicacionEntity
import com.lajara.blopps.data.remote.dto.PublicacionDto
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaInfo
import com.lajara.blopps.domain.model.Publicacion

private const val URL_IMAGENES = "https://image.tmdb.org/t/p/w500"

fun PublicacionDto.aEntity(comunidad: Comunidad): PublicacionEntity {
    return PublicacionEntity(
        id = id,
        autorId = userId,
        titulo = title.trim(),
        cuerpo = body.trim(),
        comunidad = comunidad.name
    )
}

fun PublicacionEntity.aModelo(esFavorita: Boolean = false): Publicacion {
    val pelicula = if (peliculaId != null && peliculaTitulo != null) {
        PeliculaInfo(
            id = peliculaId,
            titulo = peliculaTitulo,
            posterUrl = posterPath?.let { URL_IMAGENES + it },
            calificacion = calificacion ?: 0.0,
            anio = anio,
            sinopsis = sinopsis.orEmpty(),
            director = director
        )
    } else {
        null
    }
    return Publicacion(
        id = id,
        autorId = autorId,
        titulo = titulo,
        cuerpo = cuerpo,
        comunidad = Comunidad.entries.firstOrNull { it.name == comunidad } ?: Comunidad.ACCION,
        esFavorita = esFavorita,
        pelicula = pelicula
    )
}