package com.lajara.blopps.ui.detalle

import com.lajara.blopps.domain.model.Comentario
import com.lajara.blopps.domain.model.HiloComentario

fun armarHilos(comentarios: List<Comentario>): List<HiloComentario> {
    val raices = comentarios.filter { it.respuestaA.isNullOrBlank() }.sortedByDescending { it.fecha }
    val respuestasPorRaiz = comentarios.filter { !it.respuestaA.isNullOrBlank() }.groupBy { it.respuestaA!! }
    val idsRaices = raices.map { it.id }.toSet()

    val hilos = raices.map { raiz ->
        val resp = respuestasPorRaiz[raiz.id].orEmpty().sortedBy { it.fecha }
        HiloComentario(raiz = raiz, respuestas = resp)
    }.toMutableList()

    val hilosHuérfanos = mutableListOf<HiloComentario>()
    for ((padreId, listaResp) in respuestasPorRaiz) {
        if (padreId !in idsRaices) {
            for (resp in listaResp.sortedBy { it.fecha }) {
                hilosHuérfanos.add(HiloComentario(raiz = resp, respuestas = emptyList()))
            }
        }
    }

    return (hilos + hilosHuérfanos).sortedByDescending { it.raiz.fecha }
}
