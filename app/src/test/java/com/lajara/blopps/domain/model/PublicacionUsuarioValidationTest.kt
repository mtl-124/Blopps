package com.lajara.blopps.domain.model

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PublicacionUsuarioValidationTest {

    @Test
    fun publicacionValidaRetornaNulo() {
        val titulo = "Una película increíble"
        val resena = "Esta película tiene una trama fascinante y unas actuaciones memorables de principio a fin."
        val calificacion = 5

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNull(error)
    }

    @Test
    fun tituloDemasiadoCortoRetornaError() {
        val titulo = "Ok"
        val resena = "Esta película tiene una trama fascinante y unas actuaciones memorables de principio a fin."
        val calificacion = 4

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNotNull(error)
    }

    @Test
    fun tituloDemasiadoLargoRetornaError() {
        val titulo = "A".repeat(101)
        val resena = "Esta película tiene una trama fascinante y unas actuaciones memorables de principio a fin."
        val calificacion = 4

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNotNull(error)
    }

    @Test
    fun resenaDemasiadoCortaRetornaError() {
        val titulo = "Gran película"
        val resena = "Corta"
        val calificacion = 4

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNotNull(error)
    }

    @Test
    fun resenaDemasiadoLargaRetornaError() {
        val titulo = "Gran película"
        val resena = "A".repeat(1001)
        val calificacion = 4

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNotNull(error)
    }

    @Test
    fun calificacionInvalidaCeroRetornaError() {
        val titulo = "Gran película"
        val resena = "Esta película tiene una trama fascinante y unas actuaciones memorables de principio a fin."
        val calificacion = 0

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNotNull(error)
    }

    @Test
    fun calificacionInvalidaSeisRetornaError() {
        val titulo = "Gran película"
        val resena = "Esta película tiene una trama fascinante y unas actuaciones memorables de principio a fin."
        val calificacion = 6

        val error = validarPublicacion(titulo, resena, calificacion)
        assertNotNull(error)
    }
}
