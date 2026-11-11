package com.lajara.blopps.data.mapper

import com.lajara.blopps.data.local.PublicacionEntity
import com.lajara.blopps.data.remote.dto.MovieDto
import com.lajara.blopps.data.remote.dto.PublicacionDto
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.PeliculaInfo
import com.lajara.blopps.domain.model.Publicacion

internal const val URL_IMAGENES = "https://image.tmdb.org/t/p/w500"

internal fun extraerPosterUrl(posterPath: String?): String? {
    return posterPath?.ifBlank { null }?.let { URL_IMAGENES + it }
}

internal fun extraerAnio(releaseDate: String?): Int? {
    if (releaseDate.isNullOrBlank() || releaseDate.length < 4) return null
    val anioStr = releaseDate.take(4)
    if (!anioStr.all { it.isDigit() }) return null
    return anioStr.toIntOrNull()
}

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
            posterUrl = extraerPosterUrl(posterPath),
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

fun MovieDto.aPeliculaBusqueda(): PeliculaBusqueda {
    return PeliculaBusqueda(
        id = id,
        titulo = title,
        posterUrl = extraerPosterUrl(posterPath),
        anio = extraerAnio(releaseDate),
        calificacion = voteAverage,
        sinopsis = overview
    )
}
