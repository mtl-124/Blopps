package com.lajara.blopps.data.repository

import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Movie
import kotlinx.coroutines.flow.Flow

data class ResultadoPeliculas(
    val peliculas: List<Movie>,
    val desdeCache: Boolean
)

interface MovieRepository {
    suspend fun obtenerPeliculas(comunidad: Comunidad): Result<ResultadoPeliculas>
    suspend fun obtenerDetalle(id: String): Result<Movie>
    suspend fun sincronizar(comunidad: Comunidad): Result<Int>
    suspend fun alternarFavorito(id: String)
    fun observarFavoritos(): Flow<List<Movie>>
    fun observarConteoPorComunidad(): Flow<Map<Comunidad, Int>>
    fun observarTotalFavoritos(): Flow<Int>
    suspend fun limpiarCache()
}