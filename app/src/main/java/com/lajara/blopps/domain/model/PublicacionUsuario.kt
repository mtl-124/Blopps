package com.lajara.blopps.domain.model

object LimitesPublicacion {
    const val TITULO_MIN = 3
    const val TITULO_MAX = 100
    const val RESENA_MIN = 10
    const val RESENA_MAX = 1000
    const val CALIFICACION_MIN = 1
    const val CALIFICACION_MAX = 5
}

data class PublicacionUsuario(
    val id: String,
    val comunidad: Comunidad,
    val autorUid: String,
    val autorCorreo: String,
    val titulo: String,
    val resena: String,
    val calificacion: Int, // 1..5, la calificación que el autor le da a la película
    val peliculaId: Int,
    val peliculaTitulo: String,
    val posterUrl: String?,
    val anio: Int?,
    val fecha: Long
)

fun validarPublicacion(titulo: String, resena: String, calificacion: Int): String? {
    val tituloLimpio = titulo.trim()
    val resenaLimpia = resena.trim()
    return when {
        tituloLimpio.length !in LimitesPublicacion.TITULO_MIN..LimitesPublicacion.TITULO_MAX ->
            "El título debe tener entre ${LimitesPublicacion.TITULO_MIN} y ${LimitesPublicacion.TITULO_MAX} caracteres"
        resenaLimpia.length !in LimitesPublicacion.RESENA_MIN..LimitesPublicacion.RESENA_MAX ->
            "La reseña debe tener entre ${LimitesPublicacion.RESENA_MIN} y ${LimitesPublicacion.RESENA_MAX} caracteres"
        calificacion !in LimitesPublicacion.CALIFICACION_MIN..LimitesPublicacion.CALIFICACION_MAX ->
            "La calificación debe estar entre ${LimitesPublicacion.CALIFICACION_MIN} y ${LimitesPublicacion.CALIFICACION_MAX}"
        else -> null
    }
}
