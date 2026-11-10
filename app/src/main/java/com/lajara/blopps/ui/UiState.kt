package com.lajara.blopps.ui

import retrofit2.HttpException
import java.io.IOException

sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Exito<T>(val data: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
    data object Vacio : UiState<Nothing>
}

fun mensajeDeError(error: Throwable): String {
    return when (error) {
        is IOException -> "No hay conexión a internet"
        is HttpException -> "El servidor respondió con error ${error.code()}"
        else -> "Ocurrió un error inesperado"
    }
}