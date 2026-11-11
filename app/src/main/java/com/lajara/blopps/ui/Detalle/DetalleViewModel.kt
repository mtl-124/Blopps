package com.lajara.blopps.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.ui.UiState
import com.lajara.blopps.ui.mensajeDeError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalleViewModel(private val repo: PublicacionRepository) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<Publicacion>>(UiState.Cargando)
    val estado: StateFlow<UiState<Publicacion>> = _estado.asStateFlow()

    private var idActual: Int? = null

    fun cargar(id: Int, forzar: Boolean = false) {
        if (!forzar && id == idActual && _estado.value is UiState.Exito) return
        idActual = id
        _estado.value = UiState.Cargando
        viewModelScope.launch {
            repo.obtenerDetalle(id)
                .onSuccess { _estado.value = UiState.Exito(it) }
                .onFailure { _estado.value = UiState.Error(mensajeDeError(it)) }
        }
    }

    fun reintentar() {
        idActual?.let { cargar(it, forzar = true) }
    }

    fun alternarFavorita() {
        val actual = (_estado.value as? UiState.Exito)?.data ?: return
        viewModelScope.launch {
            repo.alternarFavorita(actual.id)
            _estado.value = UiState.Exito(actual.copy(esFavorita = !actual.esFavorita))
        }
    }
}