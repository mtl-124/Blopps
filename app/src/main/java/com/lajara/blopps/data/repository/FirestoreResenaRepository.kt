package com.lajara.blopps.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lajara.blopps.domain.model.Resena
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestoreResenaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ResenaRepository {

    private val coleccion get() = firestore.collection("resenas")

    override fun resenas(publicacionId: Int): Flow<List<Resena>> = callbackFlow {
        val registro = coleccion
            .whereEqualTo("publicacionId", publicacionId)
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = instantanea?.documents.orEmpty()
                    .mapNotNull { it.aResena() }
                    .sortedByDescending { it.fecha }
                trySend(lista)
            }
        awaitClose { registro.remove() }
    }

    override suspend fun publicar(
        publicacionId: Int,
        sesion: Sesion,
        texto: String,
        calificacion: Int
    ): Result<Unit> {
        val datos = hashMapOf(
            "publicacionId" to publicacionId,
            "autorUid" to sesion.uid,
            "autorCorreo" to sesion.correo,
            "texto" to texto.trim(),
            "calificacion" to calificacion,
            "fecha" to FieldValue.serverTimestamp()
        )
        return conTiempo { coleccion.add(datos).await() }.map { }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        return conTiempo { coleccion.document(id).delete().await() }.map { }
    }

    // Sin internet, Firestore deja la escritura en cola y nunca responde:
    // el tiempo máximo evita que la pantalla se quede cargando para siempre.
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

    private fun DocumentSnapshot.aResena(): Resena? {
        val publicacionId = getLong("publicacionId")?.toInt() ?: return null
        val autorUid = getString("autorUid") ?: return null
        val texto = getString("texto") ?: return null
        val calificacion = getLong("calificacion")?.toInt() ?: return null
        val fecha = getTimestamp("fecha", ServerTimestampBehavior.ESTIMATE)
            ?.toDate()?.time ?: System.currentTimeMillis()
        return Resena(
            id = id,
            publicacionId = publicacionId,
            autorUid = autorUid,
            autorCorreo = getString("autorCorreo").orEmpty(),
            texto = texto,
            calificacion = calificacion,
            fecha = fecha
        )
    }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 10_000L
    }
}