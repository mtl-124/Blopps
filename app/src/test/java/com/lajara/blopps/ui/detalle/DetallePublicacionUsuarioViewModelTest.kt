package com.lajara.blopps.ui.detalle

import com.lajara.blopps.data.repository.PublicacionUsuarioRepository
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.PublicacionUsuario
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

private class FakePublicacionUsuarioRepo : PublicacionUsuarioRepository {
    val flujoPublicacion = MutableStateFlow<PublicacionUsuario?>(null)
    var fallaEliminar = false

    override fun publicaciones(comunidad: Comunidad): Flow<List<PublicacionUsuario>> = flowOf(emptyList())

    override fun publicacion(id: String): Flow<PublicacionUsuario?> = flujoPublicacion

    override suspend fun crear(
        sesion: Sesion,
        comunidad: Comunidad,
        titulo: String,
        resena: String,
        calificacion: Int,
        pelicula: PeliculaBusqueda
    ): Result<String> = Result.success("1")

    override suspend fun eliminar(id: String): Result<Unit> {
        if (fallaEliminar) return Result.failure(IOException("Error al eliminar"))
        return Result.success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetallePublicacionUsuarioViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakePublicacionUsuarioRepo

    @Before
    fun preparar() {
        Dispatchers.setMain(testDispatcher)
        repo = FakePublicacionUsuarioRepo()
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargandoHastaExito() = runTest(testDispatcher) {
        val vm = DetallePublicacionUsuarioViewModel(repo)
        val pub = PublicacionUsuario(
            id = "1",
            comunidad = Comunidad.ACCION,
            autorUid = "user1",
            autorCorreo = "user1@test.com",
            titulo = "Título",
            resena = "Reseña larga",
            calificacion = 5,
            peliculaId = 1,
            peliculaTitulo = "Peli",
            posterUrl = null,
            anio = 2023,
            fecha = 1000L
        )

        vm.cargar("1")
        assertTrue(vm.estado.value is UiState.Cargando)

        repo.flujoPublicacion.value = pub
        advanceUntilIdle()

        assertTrue(vm.estado.value is UiState.Exito)
        assertEquals("Título", (vm.estado.value as UiState.Exito).data.titulo)
    }

    @Test
    fun documentoInexistenteMuestraError() = runTest(testDispatcher) {
        val vm = DetallePublicacionUsuarioViewModel(repo)

        vm.cargar("999")
        repo.flujoPublicacion.value = null
        advanceUntilIdle()

        assertTrue(vm.estado.value is UiState.Error)
        assertEquals("Esta publicación ya no existe", (vm.estado.value as UiState.Error).mensaje)
    }

    @Test
    fun reintentarTrasError() = runTest(testDispatcher) {
        val vm = DetallePublicacionUsuarioViewModel(repo)

        vm.cargar("1")
        repo.flujoPublicacion.value = null
        advanceUntilIdle()
        assertTrue(vm.estado.value is UiState.Error)

        val pub = PublicacionUsuario(
            id = "1",
            comunidad = Comunidad.ACCION,
            autorUid = "user1",
            autorCorreo = "user1@test.com",
            titulo = "Título OK",
            resena = "Reseña larga OK",
            calificacion = 5,
            peliculaId = 1,
            peliculaTitulo = "Peli",
            posterUrl = null,
            anio = 2023,
            fecha = 1000L
        )
        repo.flujoPublicacion.value = pub
        vm.cargar("1")
        advanceUntilIdle()

        assertTrue(vm.estado.value is UiState.Exito)
        assertEquals("Título OK", (vm.estado.value as UiState.Exito).data.titulo)
    }

    @Test
    fun eliminarConExito() = runTest(testDispatcher) {
        val vm = DetallePublicacionUsuarioViewModel(repo)
        repo.fallaEliminar = false

        vm.eliminar("1")
        advanceUntilIdle()

        assertTrue(vm.eliminada.value)
    }

    @Test
    fun eliminarConFallo() = runTest(testDispatcher) {
        val vm = DetallePublicacionUsuarioViewModel(repo)
        repo.fallaEliminar = true

        vm.eliminar("1")
        advanceUntilIdle()

        assertFalse(vm.eliminada.value)
    }
}
