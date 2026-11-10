package com.lajara.blopps.domain.model

data class Resena(
    val id: String,
    val publicacionId: Int,
    val autorUid: String,
    val autorCorreo: String,
    val texto: String,
    val calificacion: Int,
    val fecha: Long
)