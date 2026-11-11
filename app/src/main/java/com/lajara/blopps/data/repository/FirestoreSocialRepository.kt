package com.lajara.blopps.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.lajara.blopps.domain.model.Comentario
import com.lajara.blopps.domain.model.ResumenSocial
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestoreSocialRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SocialRepository {

    private fun publicacionRef(publicacionId: String) =
        firestore.collection("publicaciones_usuarios").document(publicacionId)

    override fun comentarios(publicacionId: String): Flow<List<Comentario>> = callbackFlow {
        val registro = publicacionRef(publicacionId)
            .collection("comentarios")
            .addSnapshotListener { instantanea, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = instantanea?.documents.orEmpty()
                    .mapNotNull { it.aComentario(publicacionId) }
                    .sortedBy { it.fecha }
                trySend(lista)
            }
        awaitClose { registro.remove() }
    }

    override fun resumen(publicacionId: String, uid: String): Flow<ResumenSocial> {
        val pubRef = publicacionRef(publicacionId)

        val votosFlow = callbackFlow {
            val reg = pubRef.collection("votos").addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snap?.documents.orEmpty()
                var suma = 0
                var cantidad = 0
                var miVoto: Int? = null

                for (doc in docs) {
                    val v = doc.getLong("valor")?.toInt() ?: continue
                    suma += v
                    cantidad++
                    if (doc.id == uid) {
                        miVoto = v
                    }
                }
                val promedio = if (cantidad > 0) suma.toDouble() / cantidad else 0.0
                trySend(Pair(promedio, cantidad to miVoto))
            }
            awaitClose { reg.remove() }
        }

        val likesFlow = callbackFlow {
            val reg = pubRef.collection("likes").addSnapshotListener { snap, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snap?.documents.orEmpty()
                val totalLikes = docs.size
                val yoDiLike = docs.any { it.id == uid }
                trySend(Pair(totalLikes, yoDiLike))
            }
            awaitClose { reg.remove() }
        }

        return combine(votosFlow, likesFlow) { votosData, likesData ->
            val (promedio, votosInfo) = votosData
            val (totalVotos, miVoto) = votosInfo
            val (totalLikes, yoDiLike) = likesData
            ResumenSocial(
                promedio = promedio,
                totalVotos = totalVotos,
                miVoto = miVoto,
                totalLikes = totalLikes,
                yoDiLike = yoDiLike
            )
        }
    }

    override suspend fun comentar(
        publicacionId: String,
        sesion: Sesion,
        texto: String,
        respuestaA: String?
    ): Result<Unit> {
        return conTiempo {
            val comentariosCol = publicacionRef(publicacionId).collection("comentarios")
            var padreId: String? = respuestaA

            if (!padreId.isNullOrBlank()) {
                val padreDoc = comentariosCol.document(padreId).get().await()
                val abueloId = padreDoc.getString("respuestaA")
                if (!abueloId.isNullOrBlank()) {
                    padreId = abueloId
                }
            }

            val datos = hashMapOf(
                "autorUid" to sesion.uid,
                "autorCorreo" to sesion.correo,
                "texto" to texto.trim(),
                "fecha" to FieldValue.serverTimestamp(),
                "respuestaA" to padreId
            )
            comentariosCol.add(datos).await()
        }.map { }
    }

    override suspend fun eliminarComentario(publicacionId: String, comentarioId: String): Result<Unit> {
        return conTiempo {
            val comentariosCol = publicacionRef(publicacionId).collection("comentarios")
            val batch = firestore.batch()
            batch.delete(comentariosCol.document(comentarioId))
            val respuestas = comentariosCol.whereEqualTo("respuestaA", comentarioId).get().await()
            for (doc in respuestas.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        }.map { }
    }

    override suspend fun votar(publicacionId: String, uid: String, valor: Int): Result<Unit> {
        return conTiempo {
            val votoRef = publicacionRef(publicacionId).collection("votos").document(uid)
            votoRef.set(hashMapOf("valor" to valor)).await()
        }.map { }
    }

    override suspend fun alternarLike(publicacionId: String, uid: String): Result<Unit> {
        return conTiempo {
            val likeRef = publicacionRef(publicacionId).collection("likes").document(uid)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(likeRef)
                if (snapshot.exists()) {
                    transaction.delete(likeRef)
                } else {
                    transaction.set(likeRef, hashMapOf("existe" to true))
                }
                null
            }.await()
        }.map { }
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

    private fun DocumentSnapshot.aComentario(publicacionId: String): Comentario? {
        val autorUid = getString("autorUid") ?: return null
        val texto = getString("texto") ?: return null
        val fecha = getTimestamp("fecha", ServerTimestampBehavior.ESTIMATE)
            ?.toDate()?.time ?: System.currentTimeMillis()
        return Comentario(
            id = id,
            publicacionId = publicacionId,
            autorUid = autorUid,
            autorCorreo = getString("autorCorreo").orEmpty(),
            texto = texto,
            fecha = fecha,
            respuestaA = getString("respuestaA")
        )
    }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 10_000L
    }
}
