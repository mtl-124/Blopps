package com.lajara.blopps.ui.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.lajara.blopps.data.auth.AuthRepository
import com.lajara.blopps.data.auth.CuentaEliminadaException
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    val modoRegistro: Boolean = false,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmacion: String? = null,
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
            val sesion = authRepository.sesionActual()
            _uiState.update { it.copy(sesion = sesion, verificandoSesion = false) }
        }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onContrasenaChange(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null, errorGeneral = null) }
    }

    fun onConfirmacionChange(valor: String) {
        _uiState.update { it.copy(confirmacion = valor, errorConfirmacion = null, errorGeneral = null) }
    }

    fun cambiarModo() {
        _uiState.update {
            LoginUiState(
                correo = it.correo,
                modoRegistro = !it.modoRegistro,
                verificandoSesion = false
            )
        }
    }

    fun enviar() {
        if (_uiState.value.modoRegistro) registrar() else iniciarSesion()
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
                        it.copy(cargando = false, errorGeneral = mensajeDeErrorLogin(error))
                    }
                }
        }
    }

    fun registrar() {
        val estado = _uiState.value
        if (estado.cargando) return

        val correo = estado.correo.trim()
        val errorCorreo = when {
            correo.isEmpty() -> "Ingresa tu correo"
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "El correo no es válido"
            else -> null
        }
        val errorContrasena = when {
            estado.contrasena.isEmpty() -> "Ingresa una contraseña"
            estado.contrasena.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        val errorConfirmacion = if (estado.confirmacion != estado.contrasena) {
            "Las contraseñas no coinciden"
        } else {
            null
        }

        if (errorCorreo != null || errorContrasena != null || errorConfirmacion != null) {
            _uiState.update {
                it.copy(
                    errorCorreo = errorCorreo,
                    errorContrasena = errorContrasena,
                    errorConfirmacion = errorConfirmacion
                )
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            authRepository.registrar(correo, estado.contrasena)
                .onSuccess { sesion ->
                    _uiState.update { it.copy(cargando = false, sesion = sesion) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(cargando = false, errorGeneral = mensajeDeErrorRegistro(error))
                    }
                }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.value = LoginUiState(verificandoSesion = false)
    }

    private fun mensajeDeErrorLogin(error: Throwable): String {
        return when (error) {
            is CuentaEliminadaException -> "Esta cuenta fue eliminada por un administrador"
            is FirebaseAuthInvalidCredentialsException,
            is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos"
            is FirebaseNetworkException -> "Sin conexión a internet"
            else -> "No se pudo iniciar sesión. Intenta de nuevo"
        }
    }

    private fun mensajeDeErrorRegistro(error: Throwable): String {
        return when (error) {
            is FirebaseAuthUserCollisionException -> "Ese correo ya está registrado"
            is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
            is FirebaseAuthInvalidCredentialsException -> "El correo no es válido"
            is FirebaseNetworkException -> "Sin conexión a internet"
            else -> "No se pudo crear la cuenta. Intenta de nuevo"
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