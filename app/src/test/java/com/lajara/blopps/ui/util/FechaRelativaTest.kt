package com.lajara.blopps.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FechaRelativaTest {

    @Test
    fun pruebaLimitesFechaRelativa() {
        val ahora = 1700000000000L // arbitrary fixed time

        // 59 segundos atrás
        val hace59s = ahora - 59 * 1000L
        assertEquals("hace un momento", fechaRelativa(ahora, hace59s))

        // 60 segundos (1 min) atrás
        val hace60s = ahora - 60 * 1000L
        assertEquals("hace 1 min", fechaRelativa(ahora, hace60s))

        // 59 minutos atrás
        val hace59m = ahora - 59 * 60 * 1000L
        assertEquals("hace 59 min", fechaRelativa(ahora, hace59m))

        // 23 horas atrás
        val hace23h = ahora - 23 * 60 * 60 * 1000L
        assertEquals("hace 23 h", fechaRelativa(ahora, hace23h))

        // 7 días atrás
        val hace7d = ahora - 7 * 24 * 60 * 60 * 1000L
        assertEquals("hace 7 d", fechaRelativa(ahora, hace7d))

        // 8 días atrás (debe retornar fecha formateada dd/MM/yyyy)
        val hace8d = ahora - 8 * 24 * 60 * 60 * 1000L
        val esperado = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(hace8d))
        assertEquals(esperado, fechaRelativa(ahora, hace8d))
    }
}
