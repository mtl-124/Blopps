package com.lajara.blopps.data.repository

import com.lajara.blopps.data.mapper.aPeliculaBusqueda
import com.lajara.blopps.data.remote.MovieApiService
import com.lajara.blopps.domain.model.PeliculaBusqueda
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PeliculaRepositoryImpl(
    private val api: MovieApiService?
) : PeliculaRepository {

    override val disponible: Boolean
        get() = api != null

    override suspend fun buscar(texto: String): Result<List<PeliculaBusqueda>> {
        val textoLimpio = texto.trim()
        if (textoLimpio.length < 2) {
            return Result.success(emptyList())
        }
        val servicio = api ?: return Result.failure(
            IllegalStateException("Búsqueda no disponible: falta la clave de TMDB")
        )
        return intentar {
            val response = servicio.buscar(textoLimpio)
            response.results
                .filter { it.title.isNotBlank() }
                .map { it.aPeliculaBusqueda() }
                .take(10)
        }
    }

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> {
        return try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        }
    }
}
