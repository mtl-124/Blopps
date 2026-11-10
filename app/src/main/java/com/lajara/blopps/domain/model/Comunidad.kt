package com.lajara.blopps.domain.model

enum class Comunidad(val titulo: String, val usuarioId: Int, val generoId: Int) {
    ACCION("Acción", 1, 28),
    COMEDIA("Comedia", 2, 35),
    CIENCIA_FICCION("Ciencia Ficción", 3, 878);

    companion object {
        fun desdeUsuario(usuarioId: Int): Comunidad? {
            return entries.firstOrNull { it.usuarioId == usuarioId }
        }
    }
}