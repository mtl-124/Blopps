package com.lajara.blopps.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.lajara.blopps.data.auth.AuthRepository
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val verificandoSesion: Boolean = true,
    val sesion: Sesion? = null
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        restaurarSesion()
    }

    private fun restaurarSesion() {
        viewModelScope.launch {
            val sesion = authRepository.obtenerSesionActual()
            _uiState.update { it.copy(sesion = sesion, verificandoSesion = false) }
        }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onContrasenaChange(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, errorGeneral = null) }
    }

    fun iniciarSesion() {
        val estado = _uiState.value
        if (estado.cargando) return

        val correo = estado.correo.trim()
        val errorCorreo = if (correo.isEmpty()) "Ingresa tu correo" else null
        val errorContrasena = if (estado.contrasena.isEmpty()) "Ingresa tu contraseña" else null

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update {
                it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena)
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            authRepository.iniciarSesion(correo, estado.contrasena)
                .onSuccess { sesion ->
                    _uiState.update { it.copy(cargando = false, sesion = sesion) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(cargando = false, errorGeneral = mensajeDeError(error))
                    }
                }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.value = LoginUiState(verificandoSesion = false)
    }

    private fun mensajeDeError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos"
            is FirebaseNetworkException -> "Sin conexión a internet"
            else -> "No se pudo iniciar sesión. Intenta de nuevo"
        }
    }
}

class LoginViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(authRepository) as T
    }
}