package com.lajara.blopps.data.auth

import com.lajara.blopps.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Sesion>
    suspend fun obtenerSesionActual(): Sesion?
    fun cerrarSesion()
}