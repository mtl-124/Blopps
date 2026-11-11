package com.lajara.blopps.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.SocialRepository
import com.lajara.blopps.domain.model.Comentario
import com.lajara.blopps.domain.model.HiloComentario
import com.lajara.blopps.domain.model.LIMITE_COMENTARIO
import com.lajara.blopps.domain.model.ResumenSocial
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SocialUiState(
    val cargando: Boolean = true,
    val hilos: List<HiloComentario> = emptyList(),
    val totalComentarios: Int = 0,
    val resumen: ResumenSocial = ResumenSocial(),
    val texto: String = "",
    val respondiendoA: Comentario? = null,
    val enviando: Boolean = false,
    val mensaje: String? = null,
    val errorCarga: String? = null
)

class SocialViewModel(
    private val socialRepo: SocialRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(SocialUiState())
    val estado: StateFlow<SocialUiState> = _estado.asStateFlow()

    private var trabajo: Job? = null
    private var publicacionActual: String? = null

    fun cargar(publicacionId: String, uid: String) {
        if (publicacionId == publicacionActual && _estado.value.errorCarga == null) return
        publicacionActual = publicacionId
        trabajo?.cancel()
        _estado.value = SocialUiState()
        trabajo = viewModelScope.launch {
            combine(
                socialRepo.comentarios(publicacionId),
                socialRepo.resumen(publicacionId, uid)
            ) { comentarios, resumen ->
                Pair(comentarios, resumen)
            }
                .catch {
                    _estado.update {
                        it.copy(cargando = false, errorCarga = "No se pudo cargar la sección social")
                    }
                }
                .collect { (comentarios, resumen) ->
                    val hilos = armarHilos(comentarios)
                    _estado.update {
                        it.copy(
                            cargando = false,
                            hilos = hilos,
                            totalComentarios = comentarios.size,
                            resumen = resumen,
                            errorCarga = null
                        )
                    }
                }
        }
    }

    fun cambiarTexto(t: String) {
        val limitado = t.take(LIMITE_COMENTARIO)
        _estado.update { it.copy(texto = limitado) }
    }

    fun responderA(c: Comentario) {
        _estado.update { it.copy(respondiendoA = c) }
    }

    fun cancelarRespuesta() {
        _estado.update { it.copy(respondiendoA = null) }
    }

    fun enviarComentario(sesion: Sesion) {
        val pubId = publicacionActual ?: return
        val estadoActual = _estado.value
        if (estadoActual.enviando) return
        val limpio = estadoActual.texto.trim()
        if (limpio.isEmpty()) {
            _estado.update { it.copy(mensaje = "Escribe un comentario antes de enviar") }
            return
        }
        if (limpio.length > LIMITE_COMENTARIO) {
            _estado.update { it.copy(mensaje = "El comentario no puede pasar de $LIMITE_COMENTARIO caracteres") }
            return
        }

        val respuestaAId = estadoActual.respondiendoA?.let { it.respuestaA ?: it.id }

        _estado.update { it.copy(enviando = true, mensaje = null) }
        viewModelScope.launch {
            socialRepo.comentar(pubId, sesion, limpio, respuestaAId)
                .onSuccess {
                    _estado.update {
                        it.copy(
                            enviando = false,
                            texto = "",
                            respondiendoA = null,
                            mensaje = null
                        )
                    }
                }
                .onFailure {
                    _estado.update {
                        it.copy(
                            enviando = false,
                            mensaje = "No se pudo comentar. Revisa tu conexión"
                        )
                    }
                }
        }
    }

    fun eliminarComentario(publicacionId: String, comentarioId: String) {
        viewModelScope.launch {
            socialRepo.eliminarComentario(publicacionId, comentarioId)
                .onFailure {
                    _estado.update { it.copy(mensaje = "No se pudo eliminar el comentario") }
                }
        }
    }

    fun votar(publicacionId: String, uid: String, valor: Int) {
        val resumenAnterior = _estado.value.resumen
        val nuevoTotalVotos = if (resumenAnterior.miVoto == null) resumenAnterior.totalVotos + 1 else resumenAnterior.totalVotos
        val sumaActual = resumenAnterior.promedio * resumenAnterior.totalVotos
        val sumaNueva = if (resumenAnterior.miVoto == null) {
            sumaActual + valor
        } else {
            sumaActual - resumenAnterior.miVoto!! + valor
        }
        val nuevoPromedio = if (nuevoTotalVotos > 0) sumaNueva / nuevoTotalVotos else 0.0
        val nuevoResumen = resumenAnterior.copy(
            promedio = nuevoPromedio,
            totalVotos = nuevoTotalVotos,
            miVoto = valor
        )
        _estado.update { it.copy(resumen = nuevoResumen) }

        viewModelScope.launch {
            socialRepo.votar(publicacionId, uid, valor)
                .onFailure {
                    _estado.update { it.copy(resumen = resumenAnterior, mensaje = "No se pudo registrar. Revisa tu conexión") }
                }
        }
    }

    fun alternarLike(publicacionId: String, uid: String) {
        val resumenAnterior = _estado.value.resumen
        val nuevoYoDiLike = !resumenAnterior.yoDiLike
        val nuevoTotalLikes = if (nuevoYoDiLike) resumenAnterior.totalLikes + 1 else maxOf(0, resumenAnterior.totalLikes - 1)
        val nuevoResumen = resumenAnterior.copy(
            yoDiLike = nuevoYoDiLike,
            totalLikes = nuevoTotalLikes
        )
        _estado.update { it.copy(resumen = nuevoResumen) }

        viewModelScope.launch {
            socialRepo.alternarLike(publicacionId, uid)
                .onFailure {
                    _estado.update { it.copy(resumen = resumenAnterior, mensaje = "No se pudo registrar. Revisa tu conexión") }
                }
        }
    }

    fun limpiarMensaje() {
        if (_estado.value.mensaje != null) {
            _estado.update { it.copy(mensaje = null) }
        }
    }
}
