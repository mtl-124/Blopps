package com.lajara.blopps.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.PublicacionUsuarioRepository
import com.lajara.blopps.domain.model.PublicacionUsuario
import com.lajara.blopps.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DetallePublicacionUsuarioViewModel(
    private val repo: PublicacionUsuarioRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UiState<PublicacionUsuario>>(UiState.Cargando)
    val estado: StateFlow<UiState<PublicacionUsuario>> = _estado.asStateFlow()

    private val _eliminando = MutableStateFlow(false)
    val eliminando: StateFlow<Boolean> = _eliminando.asStateFlow()

    private val _eliminada = MutableStateFlow(false)
    val eliminada: StateFlow<Boolean> = _eliminada.asStateFlow()

    private var trabajo: Job? = null
    private var idActual: String? = null

    fun cargar(id: String) {
        if (id == idActual && _estado.value !is UiState.Error) return
        idActual = id
        trabajo?.cancel()
        _estado.value = UiState.Cargando
        trabajo = viewModelScope.launch {
            repo.publicacion(id)
                .catch {
                    _estado.value = UiState.Error("No se pudo cargar la publicación")
                }
                .collect { pub ->
                    if (pub == null) {
                        _estado.value = UiState.Error("Esta publicación ya no existe")
                    } else {
                        _estado.value = UiState.Exito(pub)
                    }
                }
        }
    }

    fun eliminar(id: String) {
        if (_eliminando.value) return
        _eliminando.value = true
        viewModelScope.launch {
            repo.eliminar(id)
                .onSuccess {
                    _eliminando.value = false
                    _eliminada.value = true
                }
                .onFailure {
                    _eliminando.value = false
                }
        }
    }
}
