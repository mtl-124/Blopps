package com.lajara.blopps.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun fechaRelativa(ahora: Long, fecha: Long): String {
    val diff = ahora - fecha
    if (diff < 0) return "hace un momento"
    val minutos = diff / (60 * 1000)
    val horas = diff / (60 * 60 * 1000)
    val dias = diff / (24 * 60 * 60 * 1000)

    return when {
        minutos < 1 -> "hace un momento"
        minutos < 60 -> "hace $minutos min"
        horas < 24 -> "hace $horas h"
        dias <= 7 -> "hace $dias d"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fecha))
    }
}
