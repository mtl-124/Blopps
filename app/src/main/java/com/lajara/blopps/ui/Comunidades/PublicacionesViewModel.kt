package com.lajara.blopps.ui.comunidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.notification.Notificador
import com.lajara.blopps.ui.UiState
import com.lajara.blopps.ui.mensajeDeError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PublicacionesViewModel(
    private val repo: PublicacionRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val _comunidad = MutableStateFlow(Comunidad.ACCION)
    val comunidad: StateFlow<Comunidad> = _comunidad.asStateFlow()

    private val _estado = MutableStateFlow<UiState<List<Publicacion>>>(UiState.Cargando)
    val estado: StateFlow<UiState<List<Publicacion>>> = _estado.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val refrescadas = mutableSetOf<Comunidad>()

    init {
        viewModelScope.launch {
            _comunidad.flatMapLatest { repo.publicaciones(it) }.collect { lista ->
                if (lista.isNotEmpty()) {
                    _estado.value = UiState.Exito(lista)
                } else if (_comunidad.value in refrescadas) {
                    _estado.value = UiState.Vacio
                }
            }
        }
        refrescar()
    }

    fun seleccionarComunidad(nueva: Comunidad) {
        if (nueva == _comunidad.value) return
        _estado.value = UiState.Cargando
        _aviso.value = null
        _comunidad.value = nueva
        refrescar()
    }

    fun refrescar() {
        val actual = _comunidad.value
        if (_estado.value is UiState.Error) _estado.value = UiState.Cargando
        viewModelScope.launch {
            val resultado = repo.refrescar(actual)
            refrescadas.add(actual)
            if (_comunidad.value != actual) return@launch
            resultado
                .onSuccess { cantidad ->
                    _aviso.value = null
                    if (cantidad == 0 && _estado.value is UiState.Cargando) {
                        _estado.value = UiState.Vacio
                    }
                    notificador.mostrar(
                        "Datos actualizados",
                        "Se guardaron $cantidad elementos de ${actual.titulo}"
                    )
                }
                .onFailure { error ->
                    val guardadas = repo.cantidadPorComunidad()[actual] ?: 0
                    if (guardadas == 0) {
                        _estado.value = UiState.Error(mensajeDeError(error))
                    } else {
                        _aviso.value = "Sin conexión: mostrando datos guardados"
                    }
                }
        }
    }

    fun alternarFavorita(id: Int) {
        viewModelScope.launch { repo.alternarFavorita(id) }
    }
}