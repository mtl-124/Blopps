package com.lajara.blopps.data.repository

import com.lajara.blopps.data.local.FavoritoEntity
import com.lajara.blopps.data.local.PublicacionDao
import com.lajara.blopps.data.mapper.aEntity
import com.lajara.blopps.data.mapper.aModelo
import com.lajara.blopps.data.remote.MovieApiService
import com.lajara.blopps.data.remote.PublicacionApiService
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.domain.model.PublicacionPopular
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

class PublicacionRepositoryImpl(
    private val api: PublicacionApiService,
    private val peliculasApi: MovieApiService?,
    private val dao: PublicacionDao,
    private val uidActual: () -> String?
) : PublicacionRepository {

    override fun publicaciones(comunidad: Comunidad): Flow<List<Publicacion>> {
        val uid = uidActual().orEmpty()
        return combine(dao.porComunidad(comunidad.name), dao.idsFavoritos(uid)) { lista, ids ->
            val marcadas = ids.toSet()
            lista.map { it.aModelo(esFavorita = it.id in marcadas) }
        }
    }

    override val favoritas: Flow<List<Publicacion>>
        get() = dao.favoritas(uidActual().orEmpty())
            .map { lista -> lista.map { it.aModelo(esFavorita = true) } }

    override suspend fun refrescar(comunidad: Comunidad): Result<Int> {
        return intentar {
            val remotas = api.listar(comunidad.usuarioId).sortedBy { it.id }
            val peliculas = peliculasApi?.let { servicio ->
                runCatching { servicio.descubrir(comunidad.generoId).results }.getOrNull()
            }.orEmpty()
            val existentes = dao.instantanea(comunidad.name).associateBy { it.id }
            val nuevas = remotas.mapIndexed { indice, dto ->
                val nueva = dto.aEntity(comunidad)
                val base = existentes[dto.id]?.copy(
                    autorId = nueva.autorId,
                    titulo = nueva.titulo,
                    cuerpo = nueva.cuerpo
                ) ?: nueva
                val pelicula = peliculas.getOrNull(indice)
                if (pelicula == null) {
                    base
                } else {
                    base.copy(
                        peliculaId = pelicula.id,
                        peliculaTitulo = pelicula.title,
                        posterPath = pelicula.posterPath,
                        sinopsis = pelicula.overview.ifBlank { null },
                        calificacion = pelicula.voteAverage,
                        anio = pelicula.releaseDate?.take(4)?.toIntOrNull()
                    )
                }
            }
            dao.guardarTodas(nuevas)
            nuevas.size
        }
    }

    override suspend fun obtenerDetalle(id: Int): Result<Publicacion> {
        val uid = uidActual().orEmpty()
        val local = dao.porId(id)
        val remoto = intentar { api.detalle(id) }
        val dto = remoto.getOrNull()
        if (dto == null) {
            return if (local != null) {
                Result.success(local.aModelo(dao.contarFavorito(uid, id) > 0))
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

        val idPelicula = guardada.peliculaId
        val director = if (idPelicula != null && guardada.director == null) {
            buscarDirector(idPelicula)
        } else {
            null
        }
        val resultado = if (director != null) guardada.copy(director = director) else guardada

        dao.guardarTodas(listOf(resultado))
        return Result.success(resultado.aModelo(dao.contarFavorito(uid, id) > 0))
    }

    override suspend fun alternarFavorita(id: Int) {
        val uid = uidActual() ?: return
        if (dao.contarFavorito(uid, id) > 0) {
            dao.quitarFavorito(uid, id)
        } else {
            dao.agregarFavorito(FavoritoEntity(uid = uid, publicacionId = id))
        }
    }

    override suspend fun cantidadGuardada(): Int = dao.contar()

    override suspend fun cantidadFavoritas(): Int = dao.contarFavoritasTotales()

    override suspend fun cantidadPorComunidad(): Map<Comunidad, Int> {
        return Comunidad.entries.associateWith { dao.contarPorComunidad(it.name) }
    }

    override suspend fun masFavoritas(limite: Int): List<PublicacionPopular> {
        return dao.conteoFavoritos(limite).mapNotNull { conteo ->
            dao.porId(conteo.publicacionId)?.let {
                PublicacionPopular(publicacion = it.aModelo(), totalFavoritos = conteo.total)
            }
        }
    }

    override suspend fun limpiarCache() = dao.borrarTodo()

    private suspend fun buscarDirector(idPelicula: Int): String? {
        val servicio = peliculasApi ?: return null
        val detalle = runCatching { servicio.detalle(idPelicula) }.getOrNull() ?: return null
        return detalle.credits?.crew.orEmpty()
            .filter { it.job == "Director" }
            .joinToString(", ") { it.name }
            .ifBlank { null }
    }

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