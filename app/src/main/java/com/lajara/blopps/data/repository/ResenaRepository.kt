package com.lajara.blopps.data.repository

import com.lajara.blopps.domain.model.Resena
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.flow.Flow

interface ResenaRepository {
    fun resenas(publicacionId: Int): Flow<List<Resena>>
    suspend fun publicar(
        publicacionId: Int,
        sesion: Sesion,
        texto: String,
        calificacion: Int
    ): Result<Unit>
    suspend fun eliminar(id: String): Result<Unit>
}