package com.lajara.blopps.ui

sealed interface UiState<out T> {
    data object Cargando : UiState<Nothing>
    data class Exito<T>(val data: T) : UiState<T>
    data class Error(val mensaje: String) : UiState<Nothing>
    data object Vacio : UiState<Nothing>
}