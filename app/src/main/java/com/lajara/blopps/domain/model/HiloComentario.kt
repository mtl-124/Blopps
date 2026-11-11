package com.lajara.blopps.domain.model

data class HiloComentario(
    val raiz: Comentario,
    val respuestas: List<Comentario>
)
