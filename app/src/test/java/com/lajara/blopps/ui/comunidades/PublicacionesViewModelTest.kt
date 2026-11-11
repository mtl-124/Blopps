package com.lajara.blopps.ui.comunidades

import com.lajara.blopps.data.repository.PublicacionRepository
import com.lajara.blopps.data.repository.PublicacionUsuarioRepository
import com.lajara.blopps.domain.model.Comunidad
import com.lajara.blopps.domain.model.PeliculaBusqueda
import com.lajara.blopps.domain.model.Publicacion
import com.lajara.blopps.domain.model.PublicacionPopular
import com.lajara.blopps.domain.model.PublicacionUsuario
import com.lajara.blopps.domain.model.Sesion
import com.lajara.blopps.notification.Notificador
import com.lajara.blopps.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

private fun publicacion(id: Int, favorita: Boolean = false) = Publicacion(
    id = id,
    autorId = 1,
    titulo = "Titulo $id",
    cuerpo = "Cuerpo $id",
    comunidad = Comunidad.ACCION,
    esFavorita = favorita,
    pelicula = null
)

private class RepositorioFalso : PublicacionRepository {

    override suspend fun masFavoritas(limite: Int): List<PublicacionPopular> = emptyList()
    val datos = MutableStateFlow<List<Publicacion>>(emptyList())
    var remotas: List<Publicacion> = emptyList()
    var falla: Throwable? = null

    override fun publicaciones(comunidad: Comunidad): Flow<List<Publicacion>> {
        return datos.map { lista -> lista.filter { it.comunidad == comunidad } }
    }

    override val favoritas: Flow<List<Publicacion>> =
        datos.map { lista -> lista.filter { it.esFavorita } }

    override suspend fun refrescar(comunidad: Comunidad): Result<Int> {
        falla?.let { return Result.failure(it) }
        datos.value = remotas
        return Result.success(remotas.size)
    }

    override suspend fun obtenerDetalle(id: Int): Result<Publicacion> {
        val encontrada = datos.value.firstOrNull { it.id == id }
        return if (encontrada != null) Result.success(encontrada) else Result.failure(IOException())
    }

    override suspend fun alternarFavorita(id: Int) {
        datos.value = datos.value.map {
            if (it.id == id) it.copy(esFavorita = !it.esFavorita) else it
        }
    }

    override suspend fun cantidadGuardada(): Int = datos.value.size

    override suspend fun cantidadFavoritas(): Int = datos.value.count { it.esFavorita }

    override suspend fun cantidadPorComunidad(): Map<Comunidad, Int> {
        return Comunidad.entries.associateWith { c -> datos.value.count { it.comunidad == c } }
    }

    override suspend fun limpiarCache() {
        datos.value = emptyList()
    }
}

private class PublicacionUsuarioRepoFalso : PublicacionUsuarioRepository {
    var publicacionesRetornadas = listOf<PublicacionUsuario>()
    var debeLanzarError = false

    override fun publicaciones(comunidad: Comunidad): Flow<List<PublicacionUsuario>> {
        if (debeLanzarError) {
            return flow { throw IOException("Error Firestore") }
        }
        return flowOf(publicacionesRetornadas.filter { it.comunidad == comunidad })
    }

    override fun publicacion(id: String): Flow<PublicacionUsuario?> = flowOf(null)

    override suspend fun crear(
        sesion: Sesion,
        comunidad: Comunidad,
        titulo: String,
        resena: String,
        calificacion: Int,
        pelicula: PeliculaBusqueda
    ): Result<String> = Result.success("1")

    override suspend fun eliminar(id: String): Result<Unit> = Result.success(Unit)
}

private class NotificadorFalso : Notificador {
    val mensajes = mutableListOf<String>()

    override fun mostrar(titulo: String, texto: String) {
        mensajes.add(texto)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PublicacionesViewModelTest {

    private lateinit var repo: RepositorioFalso
    private lateinit var publicacionUsuarioRepo: PublicacionUsuarioRepoFalso
    private lateinit var notificador: NotificadorFalso

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repo = RepositorioFalso()
        publicacionUsuarioRepo = PublicacionUsuarioRepoFalso()
        notificador = NotificadorFalso()
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun alRefrescarConDatosMuestraListaYNotifica() {
        repo.remotas = listOf(publicacion(1), publicacion(2))

        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)

        val estado = vm.estado.value
        assertTrue(estado is UiState.Exito)
        assertEquals(2, (estado as UiState.Exito).data.size)
        assertEquals(1, notificador.mensajes.size)
    }

    @Test
    fun sinConexionYSinDatosGuardadosMuestraError() {
        repo.falla = IOException()

        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)

        val estado = vm.estado.value
        assertTrue(estado is UiState.Error)
        assertEquals("No hay conexión a internet", (estado as UiState.Error).mensaje)
        assertTrue(notificador.mensajes.isEmpty())
    }

    @Test
    fun sinConexionConDatosGuardadosMuestraListaYAviso() {
        repo.datos.value = listOf(publicacion(1))
        repo.falla = IOException()

        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)

        assertTrue(vm.estado.value is UiState.Exito)
        assertNotNull(vm.aviso.value)
    }

    @Test
    fun siNoHayPublicacionesMuestraEstadoVacio() {
        repo.remotas = emptyList()

        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)

        assertTrue(vm.estado.value is UiState.Vacio)
    }

    @Test
    fun reintentarDespuesDeUnErrorCargaLosDatos() {
        repo.falla = IOException()
        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)
        repo.falla = null
        repo.remotas = listOf(publicacion(1))

        vm.refrescar()

        assertTrue(vm.estado.value is UiState.Exito)
    }

    @Test
    fun deUsuariosReflejaListaDeRepositorioFalso() {
        val pubUsuario = PublicacionUsuario(
            id = "u1",
            comunidad = Comunidad.ACCION,
            autorUid = "user1",
            autorCorreo = "user1@test.com",
            titulo = "Gran peli de acción",
            resena = "Una reseña muy larga sobre la película de acción.",
            calificacion = 5,
            peliculaId = 1,
            peliculaTitulo = "Action Movie",
            posterUrl = null,
            anio = 2023,
            fecha = System.currentTimeMillis()
        )
        publicacionUsuarioRepo.publicacionesRetornadas = listOf(pubUsuario)

        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)

        assertEquals(1, vm.deUsuarios.value.size)
        assertEquals("Gran peli de acción", vm.deUsuarios.value.first().titulo)
    }

    @Test
    fun siRepositorioFallaDeUsuariosEsListaVacia() {
        publicacionUsuarioRepo.debeLanzarError = true

        val vm = PublicacionesViewModel(repo, publicacionUsuarioRepo, notificador)

        assertTrue(vm.deUsuarios.value.isEmpty())
    }
}
