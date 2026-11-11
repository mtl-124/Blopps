package com.lajara.blopps.domain.model

data class ResumenSocial(
    val promedio: Double = 0.0,
    val totalVotos: Int = 0,
    val miVoto: Int? = null,
    val totalLikes: Int = 0,
    val yoDiLike: Boolean = false
)
