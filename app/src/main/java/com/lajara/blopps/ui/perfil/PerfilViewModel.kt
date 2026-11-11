package com.lajara.blopps.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.auth.AuthRepository
import com.lajara.blopps.data.repository.PublicacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val favoritas: Int = 0,
    val guardadas: Int = 0,
    val enviandoCorreo: Boolean = false,
    val mensaje: String? = null
)

class PerfilViewModel(
    private val auth: AuthRepository,
    private val repo: PublicacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState())
    val estado: StateFlow<PerfilUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            repo.favoritas.collect { lista ->
                _estado.update { it.copy(favoritas = lista.size) }
            }
        }
        actualizarGuardadas()
    }

    fun actualizarGuardadas() {
        viewModelScope.launch {
            val total = repo.cantidadGuardada()
            _estado.update { it.copy(guardadas = total) }
        }
    }

    fun restablecerContrasena(correo: String) {
        if (_estado.value.enviandoCorreo) return
        _estado.update { it.copy(enviandoCorreo = true, mensaje = null) }
        viewModelScope.launch {
            val resultado = auth.enviarCorreoRestablecimiento(correo)
            _estado.update { actual ->
                resultado.fold(
                    onSuccess = {
                        actual.copy(
                            enviandoCorreo = false,
                            mensaje = "Te enviamos un correo para restablecer tu contraseña"
                        )
                    },
                    onFailure = {
                        actual.copy(
                            enviandoCorreo = false,
                            mensaje = "No se pudo enviar el correo. Revisa tu conexión"
                        )
                    }
                )
            }
        }
    }

    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }
}