package com.lajara.blopps.ui.crear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lajara.blopps.data.repository.PeliculaRepository
import com.lajara.blopps.data.repository.PublicacionUsuarioRepository
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.LimitesPublicacion
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class CrearUiState(
    val comunidad: Comunidad? = null,
    val consulta: String = "",
    val resultados: List<PeliculaBusqueda> = emptyList(),
    val buscando: Boolean = false,
    val errorBusqueda: String? = null,
    val busquedaDisponible: Boolean = true,
    val peliculaElegida: PeliculaBusqueda? = null,
    val titulo: String = "",
    val resena: String = "",
    val calificacion: Int = 0,
    val publicando: Boolean = false,
    val mensaje: String? = null,
    val publicada: Boolean = false
)

class CrearPublicacionViewModel(
    private val peliculaRepo: PeliculaRepository,
    private val publicacionRepo: PublicacionUsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearUiState(busquedaDisponible = peliculaRepo.disponible))
    val uiState: StateFlow<CrearUiState> = _uiState.asStateFlow()

    private val consultaFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            consultaFlow
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { texto ->
                    val textoLimpio = texto.trim()
                    if (textoLimpio.length < 2) {
                        _uiState.update {
                            it.copy(
                                resultados = emptyList(),
                                buscando = false,
                                errorBusqueda = null
                            )
                        }
                        return@collectLatest
                    }
                    if (!peliculaRepo.disponible) {
                        _uiState.update {
                            it.copy(
                                buscando = false,
                                errorBusqueda = "La búsqueda de películas no está disponible"
                            )
                        }
                        return@collectLatest
                    }
                    _uiState.update { it.copy(buscando = true, errorBusqueda = null) }
                    val resultado = peliculaRepo.buscar(textoLimpio)
                    resultado.fold(
                        onSuccess = { lista ->
                            _uiState.update {
                                it.copy(
                                    resultados = lista,
                                    buscando = false,
                                    errorBusqueda = null
                                )
                            }
                        },
                        onFailure = { error ->
                            val mensajeError = when (error) {
                                is IOException -> "No hay conexión a internet"
                                is HttpException -> "El servidor respondió con error ${error.code()}"
                                is IllegalStateException -> error.message ?: "La búsqueda de películas no está disponible"
                                else -> "Ocurrió un error inesperado"
                            }
                            _uiState.update {
                                it.copy(
                                    resultados = emptyList(),
                                    buscando = false,
                                    errorBusqueda = mensajeError
                                )
                            }
                        }
                    )
                }
        }
    }

    fun elegirComunidad(c: Comunidad) {
        _uiState.update { it.copy(comunidad = c) }
    }

    fun cambiarConsulta(texto: String) {
        _uiState.update { it.copy(consulta = texto) }
        consultaFlow.value = texto
    }

    fun elegirPelicula(p: PeliculaBusqueda) {
        _uiState.update {
            it.copy(
                peliculaElegida = p,
                consulta = "",
                resultados = emptyList(),
                buscando = false,
                errorBusqueda = null
            )
        }
    }

    fun quitarPelicula() {
        _uiState.update { it.copy(peliculaElegida = null) }
    }

    fun cambiarTitulo(t: String) {
        val limitado = t.take(LimitesPublicacion.TITULO_MAX)
        _uiState.update { it.copy(titulo = limitado) }
    }

    fun cambiarResena(r: String) {
        val limitado = r.take(LimitesPublicacion.RESENA_MAX)
        _uiState.update { it.copy(resena = limitado) }
    }

    fun elegirCalificacion(n: Int) {
        _uiState.update { it.copy(calificacion = n) }
    }

    fun publicar(sesion: Sesion) {
        val estado = _uiState.value
        if (estado.publicando) return

        val mensajeError = when {
            estado.comunidad == null -> "Elige una comunidad"
            estado.peliculaElegida == null -> "Elige una película"
            estado.titulo.trim().length !in LimitesPublicacion.TITULO_MIN..LimitesPublicacion.TITULO_MAX ->
                "El título debe tener entre ${LimitesPublicacion.TITULO_MIN} y ${LimitesPublicacion.TITULO_MAX} caracteres"
            estado.resena.trim().length !in LimitesPublicacion.RESENA_MIN..LimitesPublicacion.RESENA_MAX ->
                "La reseña debe tener entre ${LimitesPublicacion.RESENA_MIN} y ${LimitesPublicacion.RESENA_MAX} caracteres"
            estado.calificacion !in LimitesPublicacion.CALIFICACION_MIN..LimitesPublicacion.CALIFICACION_MAX ->
                "Elige una calificación de 1 a 5 estrellas"
            else -> null
        }

        if (mensajeError != null) {
            _uiState.update { it.copy(mensaje = mensajeError) }
            return
        }

        val comunidad = estado.comunidad ?: return
        val pelicula = estado.peliculaElegida ?: return

        _uiState.update { it.copy(publicando = true) }
        viewModelScope.launch {
            val resultado = publicacionRepo.crear(
                sesion = sesion,
                comunidad = comunidad,
                titulo = estado.titulo,
                resena = estado.resena,
                calificacion = estado.calificacion,
                pelicula = pelicula
            )
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(publicando = false, publicada = true) }
                },
                onFailure = {
                    _uiState.update { it.copy(publicando = false, mensaje = "No se pudo publicar. Revisa tu conexión") }
                }
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun consumirPublicada() {
        _uiState.update { it.copy(publicada = false) }
    }
}
