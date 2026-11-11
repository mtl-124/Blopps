package com.lajara.blopps.data.repository

import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.PublicacionUsuario
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.flow.Flow

interface PublicacionUsuarioRepository {
    fun publicaciones(comunidad: Comunidad): Flow<List<PublicacionUsuario>>
    fun publicacion(id: String): Flow<PublicacionUsuario?>
    suspend fun crear(
        sesion: Sesion,
        comunidad: Comunidad,
        titulo: String,
        resena: String,
        calificacion: Int,
        pelicula: PeliculaBusqueda
    ): Result<String>
    suspend fun eliminar(id: String): Result<Unit>
}
