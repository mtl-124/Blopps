package com.lajara.blopps.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PublicacionPopular
import com.lajara.blopps.notification.Notificador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val totalGuardadas: Int = 0,
    val totalFavoritas: Int = 0,
    val porComunidad: Map<Comunidad, Int> = emptyMap(),
    val masFavoritas: List<PublicacionPopular> = emptyList(),
    val ultimaSincronizacion: Long? = null,
    val sincronizando: Boolean = false,
    val mensaje: String? = null
)

class AdminViewModel(
    private val repo: PublicacionRepository,
    private val notificador: Notificador
) : ViewModel() {

    private val _estado = MutableStateFlow(AdminUiState())
    val estado: StateFlow<AdminUiState> = _estado.asStateFlow()

    init {
        actualizarMetricas()
    }

    fun actualizarMetricas() {
        viewModelScope.launch { cargarMetricas() }
    }

    fun sincronizar() {
        if (_estado.value.sincronizando) return
        _estado.update { it.copy(sincronizando = true, mensaje = null) }
        viewModelScope.launch {
            var total = 0
            var huboError = false
            for (comunidad in Comunidad.entries) {
                repo.refrescar(comunidad)
                    .onSuccess { total += it }
                    .onFailure { huboError = true }
            }
            cargarMetricas()
            if (huboError) {
                _estado.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "No se pudo sincronizar todo. Revisa tu conexión"
                    )
                }
            } else {
                notificador.mostrar("Datos actualizados", "Se guardaron $total elementos")
                _estado.update {
                    it.copy(
                        sincronizando = false,
                        mensaje = "Sincronización completa",
                        ultimaSincronizacion = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun sincronizarComunidad(comunidad: Comunidad) {
        if (_estado.value.sincronizando) return
        _estado.update { it.copy(sincronizando = true, mensaje = null) }
        viewModelScope.launch {
            val resultado = repo.refrescar(comunidad)
            cargarMetricas()
            _estado.update { actual ->
                resultado.fold(
                    onSuccess = { cantidad ->
                        actual.copy(
                            sincronizando = false,
                            mensaje = "${comunidad.titulo}: $cantidad reseñas actualizadas",
                            ultimaSincronizacion = System.currentTimeMillis()
                        )
                    },
                    onFailure = {
                        actual.copy(
                            sincronizando = false,
                            mensaje = "No se pudo sincronizar ${comunidad.titulo}. Revisa tu conexión"
                        )
                    }
                )
            }
        }
    }

    fun limpiarCache() {
        viewModelScope.launch {
            repo.limpiarCache()
            cargarMetricas()
            _estado.update { it.copy(mensaje = "Caché limpiada") }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    private suspend fun cargarMetricas() {
        val guardadas = repo.cantidadGuardada()
        val favoritas = repo.cantidadFavoritas()
        val porComunidad = repo.cantidadPorComunidad()
        val populares = repo.masFavoritas(5)
        _estado.update {
            it.copy(
                totalGuardadas = guardadas,
                totalFavoritas = favoritas,
                porComunidad = porComunidad,
                masFavoritas = populares
            )
        }
    }
}