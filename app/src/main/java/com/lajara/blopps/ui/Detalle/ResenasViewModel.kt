package com.lajara.blopps.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.ResenaRepository
import com.lajara.blopps.domain.model.Resena
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val LIMITE_RESENA = 1000

data class ResenasUiState(
    val cargando: Boolean = true,
    val resenas: List<Resena> = emptyList(),
    val errorCarga: String? = null,
    val publicando: Boolean = false,
    val mensaje: String? = null
)

class ResenasViewModel(private val repo: ResenaRepository) : ViewModel() {

    private val _estado = MutableStateFlow(ResenasUiState())
    val estado: StateFlow<ResenasUiState> = _estado.asStateFlow()

    private var trabajo: Job? = null
    private var publicacionActual: Int? = null

    fun cargar(publicacionId: Int) {
        if (publicacionId == publicacionActual) return
        publicacionActual = publicacionId
        trabajo?.cancel()
        _estado.value = ResenasUiState()
        trabajo = viewModelScope.launch {
            repo.resenas(publicacionId)
                .catch {
                    _estado.update {
                        it.copy(cargando = false, errorCarga = "No se pudieron cargar las reseñas")
                    }
                }
                .collect { lista ->
                    _estado.update {
                        it.copy(cargando = false, errorCarga = null, resenas = lista)
                    }
                }
        }
    }

    fun publicar(sesion: Sesion, texto: String, calificacion: Int, alExito: () -> Unit) {
        val publicacionId = publicacionActual ?: return
        if (_estado.value.publicando) return
        val limpio = texto.trim()
        if (calificacion !in 1..5) {
            mostrar("Elige una calificación de 1 a 5 estrellas")
            return
        }
        if (limpio.isEmpty()) {
            mostrar("Escribe tu reseña antes de publicar")
            return
        }
        if (limpio.length > LIMITE_RESENA) {
            mostrar("La reseña no puede pasar de $LIMITE_RESENA caracteres")
            return
        }

        _estado.update { it.copy(publicando = true, mensaje = null) }
        viewModelScope.launch {
            repo.publicar(publicacionId, sesion, limpio, calificacion)
                .onSuccess {
                    _estado.update { it.copy(publicando = false, mensaje = null) }
                    alExito()
                }
                .onFailure {
                    _estado.update {
                        it.copy(
                            publicando = false,
                            mensaje = "No se pudo publicar. Revisa tu conexión"
                        )
                    }
                }
        }
    }

    fun eliminar(resena: Resena) {
        viewModelScope.launch {
            repo.eliminar(resena.id)
                .onFailure { mostrar("No se pudo eliminar la reseña") }
        }
    }

    fun limpiarMensaje() {
        if (_estado.value.mensaje != null) {
            _estado.update { it.copy(mensaje = null) }
        }
    }

    private fun mostrar(texto: String) {
        _estado.update { it.copy(mensaje = texto) }
    }
}