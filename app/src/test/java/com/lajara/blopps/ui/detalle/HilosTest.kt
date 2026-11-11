package com.lajara.blopps.ui.detalle

import com.lajara.blopps.domain.model.Comentario
import org.junit.Assert.assertEquals
import org.junit.Test

class HilosTest {

    @Test
    fun listaVaciaDevuelveHilosVacios() {
        val hilos = armarHilos(emptyList())
        assertEquals(0, hilos.size)
    }

    @Test
    fun raicesOrdenadasDescendenteYRespuestasAscendente() {
        val c1 = Comentario("1", "p1", "u1", "u1@test.com", "Raíz antigua", 1000L, null)
        val c2 = Comentario("2", "p1", "u2", "u2@test.com", "Raíz nueva", 2000L, null)
        val r1 = Comentario("3", "p1", "u3", "u3@test.com", "Respuesta vieja a c2", 2100L, "2")
        val r2 = Comentario("4", "p1", "u4", "u4@test.com", "Respuesta nueva a c2", 2200L, "2")

        val hilos = armarHilos(listOf(c1, c2, r1, r2))

        assertEquals(2, hilos.size)
        // Raíz nueva (2000L) debe ir primero
        assertEquals("2", hilos[0].raiz.id)
        assertEquals(2, hilos[0].respuestas.size)
        // Respuestas ordenadas ascendente
        assertEquals("3", hilos[0].respuestas[0].id)
        assertEquals("4", hilos[0].respuestas[1].id)

        // Raíz antigua (1000L) segundo
        assertEquals("1", hilos[1].raiz.id)
        assertEquals(0, hilos[1].respuestas.size)
    }

    @Test
    fun respuestaHuerfanaSeTrataComoRaiz() {
        val huérfana = Comentario("10", "p1", "u1", "u1@test.com", "Huérfana", 1500L, "no_existe")

        val hilos = armarHilos(listOf(huérfana))

        assertEquals(1, hilos.size)
        assertEquals("10", hilos[0].raiz.id)
        assertEquals(0, hilos[0].respuestas.size)
    }
}
