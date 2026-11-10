package com.lajara.blopps.data.repository

import com.lajara.blopps.data.local.PublicacionDao
import com.lajara.blopps.data.mapper.aEntity
import com.lajara.blopps.data.mapper.aModelo
import com.lajara.blopps.data.remote.PublicacionApiService
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Publicacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PublicacionRepositoryImpl(
    private val api: PublicacionApiService,
    private val dao: PublicacionDao
) : PublicacionRepository {

    override fun publicaciones(comunidad: Comunidad): Flow<List<Publicacion>> {
        return dao.porComunidad(comunidad.name).map { lista -> lista.map { it.aModelo() } }
    }

    override val favoritas: Flow<List<Publicacion>> =
        dao.favoritas().map { lista -> lista.map { it.aModelo() } }

    override suspend fun refrescar(comunidad: Comunidad): Result<Int> {
        return intentar {
            val remotas = api.listar(comunidad.usuarioId)
            val existentes = dao.instantanea(comunidad.name).associateBy { it.id }
            val nuevas = remotas.map { dto ->
                val nueva = dto.aEntity(comunidad)
                val previa = existentes[dto.id]
                previa?.copy(
                    autorId = nueva.autorId,
                    titulo = nueva.titulo,
                    cuerpo = nueva.cuerpo
                ) ?: nueva
            }
            dao.guardarTodas(nuevas)
            nuevas.size
        }
    }

    override suspend fun obtenerDetalle(id: Int): Result<Publicacion> {
        val local = dao.porId(id)
        val remoto = intentar { api.detalle(id) }
        val dto = remoto.getOrNull()
        if (dto == null) {
            return if (local != null) {
                Result.success(local.aModelo())
            } else {
                Result.failure(
                    remoto.exceptionOrNull() ?: IllegalStateException("Publicación no disponible")
                )
            }
        }
        val comunidad = Comunidad.desdeUsuario(dto.userId)
            ?: return Result.failure(IllegalStateException("Comunidad desconocida"))
        val nueva = dto.aEntity(comunidad)
        val guardada = local?.copy(
            autorId = nueva.autorId,
            titulo = nueva.titulo,
            cuerpo = nueva.cuerpo
        ) ?: nueva
        dao.guardarTodas(listOf(guardada))
        return Result.success(guardada.aModelo())
    }

    override suspend fun alternarFavorita(id: Int) = dao.alternarFavorita(id)

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritas(): Int = dao.contarFavoritas()

    override suspend fun cantidadPorComunidad(): Map<Comunidad, Int> {
        return Comunidad.entries.associateWith { dao.contarPorComunidad(it.name) }
    }

    override suspend fun limpiarCache() = dao.borrarTodo()

    private suspend fun <T> intentar(bloque: suspend () -> T): Result<T> {
        return try {
            Result.success(bloque())
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        }
    }
}