package com.lajara.blopps.notification

import com.lajara.blopps.domain.model.Comunidad

interface Notificador {
    fun notificarSincronizacion(cantidad: Int, comunidad: Comunidad)
}