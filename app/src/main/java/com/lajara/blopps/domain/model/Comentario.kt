package com.lajara.blopps.domain.model

const val LIMITE_COMENTARIO = 500

data class Comentario(
    val id: String,
    val publicacionId: String,
    val autorUid: String,
    val autorCorreo: String,
    val texto: String,
    val fecha: Long,
    val respuestaA: String?
)
