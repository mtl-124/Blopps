package com.lajara.blopps.ui.crear

import com.lajara.blopps.data.repository.PeliculaRepository
import com.lajara.blopps.data.repository.PublicacionUsuarioRepository
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.PublicacionUsuario
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

private class PeliculaRepoFalso(
    override val disponible: Boolean = true,
    var resultados: List<PeliculaBusqueda> = emptyList(),
    var falla: Throwable? = null
) : PeliculaRepository {
    var llamadasBusqueda = 0

    override suspend fun buscar(texto: String): Result<List<PeliculaBusqueda>> {
        llamadasBusqueda++
        falla?.let { return Result.failure(it) }
        return Result.success(resultados)
    }
}

private class PublicacionUsuarioRepoFalso : PublicacionUsuarioRepository {
    var vecesCreado = 0
    var fallaCreacion: Throwable? = null

    override fun publicaciones(comunidad: Comunidad): Flow<List<PublicacionUsuario>> = flowOf(emptyList())
    override fun publicacion(id: String): Flow<PublicacionUsuario?> = flowOf(null)

    override suspend fun crear(
        sesion: Sesion,
        comunidad: Comunidad,
        titulo: String,
        resena: String,
        calificacion: Int,
        pelicula: PeliculaBusqueda
    ): Result<String> {
        delay(10)
        vecesCreado++
        fallaCreacion?.let { return Result.failure(it) }
        return Result.success("doc_id_123")
    }

    override suspend fun eliminar(id: String): Result<Unit> = Result.success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class CrearPublicacionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var peliculaRepo: PeliculaRepoFalso
    private lateinit var publicacionRepo: PublicacionUsuarioRepoFalso
    private lateinit var sesion: Sesion

    @Before
    fun preparar() {
        Dispatchers.setMain(testDispatcher)
        peliculaRepo = PeliculaRepoFalso()
        publicacionRepo = PublicacionUsuarioRepoFalso()
        sesion = Sesion(uid = "user1", correo = "test@test.com", rol = Rol.USUARIO)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun consultaDeUnCaracterNoLlamaApi() = runTest(testDispatcher) {
        val vm = CrearPublicacionViewModel(peliculaRepo, publicacionRepo)
        vm.cambiarConsulta("a")
        testDispatcher.scheduler.advanceTimeBy(600)

        assertEquals(0, peliculaRepo.llamadasBusqueda)
        assertTrue(vm.uiState.value.resultados.isEmpty())
    }

    @Test
    fun debounceDosCambiosRapidosLlamanUnaSolaVez() = runTest(testDispatcher) {
        peliculaRepo.resultados = listOf(PeliculaBusqueda(1, "Matrix", null, 1999, 8.5, "Sinopsis"))
        val vm = CrearPublicacionViewModel(peliculaRepo, publicacionRepo)

        vm.cambiarConsulta("Mat")
        testDispatcher.scheduler.advanceTimeBy(200)
        vm.cambiarConsulta("Matrix")
        testDispatcher.scheduler.advanceTimeBy(600)

        assertEquals(1, peliculaRepo.llamadasBusqueda)
        assertEquals(1, vm.uiState.value.resultados.size)
    }

    @Test
    fun validacionesDePublicarProducenMensajesExactos() = runTest(testDispatcher) {
        val vm = CrearPublicacionViewModel(peliculaRepo, publicacionRepo)

        // 1. Sin comunidad
        vm.publicar(sesion)
        assertEquals("Elige una comunidad", vm.uiState.value.mensaje)

        // 2. Sin película
        vm.elegirComunidad(Comunidad.ACCION)
        vm.publicar(sesion)
        assertEquals("Elige una película", vm.uiState.value.mensaje)

        // 3. Título inválido
        vm.elegirPelicula(PeliculaBusqueda(1, "Matrix", null, 1999, 8.5, "Sinopsis"))
        vm.cambiarTitulo("Hi")
        vm.publicar(sesion)
        assertEquals("El título debe tener entre 3 y 100 caracteres", vm.uiState.value.mensaje)

        // 4. Reseña inválida
        vm.cambiarTitulo("Título válido")
        vm.cambiarResena("Corta")
        vm.publicar(sesion)
        assertEquals("La reseña debe tener entre 10 y 1000 caracteres", vm.uiState.value.mensaje)

        // 5. Calificación inválida
        vm.cambiarResena("Esta es una reseña suficientemente larga para la prueba.")
        vm.elegirCalificacion(0)
        vm.publicar(sesion)
        assertEquals("Elige una calificación de 1 a 5 estrellas", vm.uiState.value.mensaje)
    }

    @Test
    fun dobleToqueEnPublicarSoloLlamaCrearUnaVez() = runTest(testDispatcher) {
        val vm = CrearPublicacionViewModel(peliculaRepo, publicacionRepo)
        vm.elegirComunidad(Comunidad.ACCION)
        vm.elegirPelicula(PeliculaBusqueda(1, "Matrix", null, 1999, 8.5, "Sinopsis"))
        vm.cambiarTitulo("Título válido")
        vm.cambiarResena("Esta es una reseña suficientemente larga para la prueba.")
        vm.elegirCalificacion(5)

        // Simular publicación concurrente / doble toque
        vm.publicar(sesion)
        vm.publicar(sesion)
        testScheduler.advanceUntilIdle()

        assertEquals(1, publicacionRepo.vecesCreado)
    }

    @Test
    fun exitoMarcaPublicadaTrue() = runTest(testDispatcher) {
        val vm = CrearPublicacionViewModel(peliculaRepo, publicacionRepo)
        vm.elegirComunidad(Comunidad.ACCION)
        vm.elegirPelicula(PeliculaBusqueda(1, "Matrix", null, 1999, 8.5, "Sinopsis"))
        vm.cambiarTitulo("Título válido")
        vm.cambiarResena("Esta es una reseña suficientemente larga para la prueba.")
        vm.elegirCalificacion(5)

        vm.publicar(sesion)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.publicada)
        assertFalse(vm.uiState.value.publicando)
    }

    @Test
    fun falloMuestraMensajeYPublicandoVuelveFalse() = runTest(testDispatcher) {
        publicacionRepo.fallaCreacion = IOException("Error")
        val vm = CrearPublicacionViewModel(peliculaRepo, publicacionRepo)
        vm.elegirComunidad(Comunidad.ACCION)
        vm.elegirPelicula(PeliculaBusqueda(1, "Matrix", null, 1999, 8.5, "Sinopsis"))
        vm.cambiarTitulo("Título válido")
        vm.cambiarResena("Esta es una reseña suficientemente larga para la prueba.")
        vm.elegirCalificacion(5)

        vm.publicar(sesion)
        testScheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.publicada)
        assertFalse(vm.uiState.value.publicando)
        assertEquals("No se pudo publicar. Revisa tu conexión", vm.uiState.value.mensaje)
    }
}
