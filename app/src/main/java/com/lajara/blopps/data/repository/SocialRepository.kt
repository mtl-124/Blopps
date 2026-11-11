package com.lajara.blopps.data.repository

import com.lajara.blopps.domain.model.Comentario
import com.lajara.blopps.domain.model.ResumenSocial
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun comentarios(publicacionId: String): Flow<List<Comentario>>
    fun resumen(publicacionId: String, uid: String): Flow<ResumenSocial>
    suspend fun comentar(publicacionId: String, sesion: Sesion, texto: String, respuestaA: String?): Result<Unit>
    suspend fun eliminarComentario(publicacionId: String, comentarioId: String): Result<Unit>
    suspend fun votar(publicacionId: String, uid: String, valor: Int): Result<Unit>
    suspend fun alternarLike(publicacionId: String, uid: String): Result<Unit>
}
