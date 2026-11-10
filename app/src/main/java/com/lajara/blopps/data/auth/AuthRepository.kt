package com.lajara.blopps.data.auth

import com.lajara.blopps.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion>
    suspend fun registrar(correo: String, clave: String): Result<Sesion>
    suspend fun sesionActual(): Sesion?
    suspend fun enviarCorreoRestablecimiento(correo: String): Result<Unit>
    fun cerrarSesion()
}