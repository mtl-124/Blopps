package com.lajara.blopps.data.repository

import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Publicacion
import kotlinx.coroutines.flow.Flow

interface PublicacionRepository {
    fun publicaciones(comunidad: Comunidad): Flow<List<Publicacion>>
    val favoritas: Flow<List<Publicacion>>
    suspend fun refrescar(comunidad: Comunidad): Result<Int>
    suspend fun obtenerDetalle(id: Int): Result<Publicacion>
    suspend fun alternarFavorita(id: Int)
    suspend fun cantidadGuardada(): Int
    suspend fun cantidadFavoritas(): Int
    suspend fun cantidadPorComunidad(): Map<Comunidad, Int>
    suspend fun limpiarCache()
}