package com.lajara.blopps.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.PublicacionUsuario
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestorePublicacionUsuarioRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PublicacionUsuarioRepository {

    private val coleccion get() = firestore.collection("publicaciones_usuarios")

    override fun publicaciones(comunidad: Comunidad): Flow<List<PublicacionUsuario>> = callbackFlow {
        val registro = coleccion
            .whereEqualTo("comunidad", comunidad.name)
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = instantanea?.documents.orEmpty()
                    .mapNotNull { it.aPublicacionUsuario() }
                    .sortedByDescending { it.fecha }
                trySend(lista)
            }
        awaitClose { registro.remove() }
    }

    override fun publicacion(id: String): Flow<PublicacionUsuario?> = callbackFlow {
        val registro = coleccion.document(id)
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val pub = instantanea?.takeIf { it.exists() }?.aPublicacionUsuario()
                trySend(pub)
            }
        awaitClose { registro.remove() }
    }

    override suspend fun crear(
        sesion: Sesion,
        comunidad: Comunidad,
        titulo: String,
        resena: String,
        calificacion: Int,
        pelicula: PeliculaBusqueda
    ): Result<String> {
        val datos = hashMapOf(
            "comunidad" to comunidad.name,
            "autorUid" to sesion.uid,
            "autorCorreo" to sesion.correo,
            "titulo" to titulo.trim(),
            "resena" to resena.trim(),
            "calificacion" to calificacion,
            "peliculaId" to pelicula.id,
            "peliculaTitulo" to pelicula.titulo,
            "posterUrl" to pelicula.posterUrl,
            "anio" to pelicula.anio,
            "fecha" to FieldValue.serverTimestamp()
        )
        return conTiempo {
            val ref = coleccion.add(datos).await()
            ref.id
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return conTiempo { coleccion.document(id).delete().await() }.map { }
    }

    private suspend fun <T> conTiempo(bloque: suspend () -> T): Result<T> {
        return try {
            Result.success(withTimeout(TIEMPO_MAXIMO_MS) { bloque() })
        } catch (e: TimeoutCancellationException) {
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.aPublicacionUsuario(): PublicacionUsuario? {
        val comunidadStr = getString("comunidad") ?: return null
        val comunidad = Comunidad.entries.firstOrNull { it.name == comunidadStr } ?: return null
        val autorUid = getString("autorUid") ?: return null
        val titulo = getString("titulo") ?: return null
        val resena = getString("resena") ?: return null
        val calificacion = getLong("calificacion")?.toInt() ?: return null
        val peliculaId = getLong("peliculaId")?.toInt() ?: return null
        val peliculaTitulo = getString("peliculaTitulo") ?: return null
        val fecha = getTimestamp("fecha", ServerTimestampBehavior.ESTIMATE)
            ?.toDate()?.time ?: System.currentTimeMillis()

        return PublicacionUsuario(
            id = id,
            comunidad = comunidad,
            autorUid = autorUid,
            autorCorreo = getString("autorCorreo").orEmpty(),
            titulo = titulo,
            resena = resena,
            calificacion = calificacion,
            peliculaId = peliculaId,
            peliculaTitulo = peliculaTitulo,
            posterUrl = getString("posterUrl"),
            anio = getLong("anio")?.toInt(),
            fecha = fecha
        )
    }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 10_000L
    }
}
