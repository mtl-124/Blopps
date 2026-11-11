package com.lajara.blopps.ui.detalle

import com.lajara.blopps.data.repository.SocialRepository
import com.lajara.blopps.domain.model.Comentario
import com.lajara.blopps.domain.model.ResumenSocial
import com.lajara.blopps.domain.model.Rol
import com.lajara.blopps.domain.model.Sesion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

private class FakeSocialRepository : SocialRepository {
    var comentariosRetornados = listOf<Comentario>()
    var resumenRetornado = ResumenSocial()
    var vecesComentado = 0
    var fallaComentar: Throwable? = null
    var fallaLike: Throwable? = null
    var ultimoRespuestaA: String? = null

    override fun comentarios(publicacionId: String): Flow<List<Comentario>> = flowOf(comentariosRetornados)

    override fun resumen(publicacionId: String, uid: String): Flow<ResumenSocial> = flowOf(resumenRetornado)

    override suspend fun comentar(
        publicacionId: String,
        sesion: Sesion,
        texto: String,
        respuestaA: String?
    ): Result<Unit> {
        vecesComentado++
        ultimoRespuestaA = respuestaA
        fallaComentar?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    override suspend fun eliminarComentario(publicacionId: String, comentarioId: String): Result<Unit> = Result.success(Unit)

    override suspend fun votar(publicacionId: String, uid: String, valor: Int): Result<Unit> = Result.success(Unit)

    override suspend fun alternarLike(publicacionId: String, uid: String): Result<Unit> {
        fallaLike?.let { return Result.failure(it) }
        return Result.success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SocialViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeSocialRepository
    private lateinit var sesion: Sesion

    @Before
    fun preparar() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeSocialRepository()
        sesion = Sesion(uid = "user1", correo = "test@test.com", rol = Rol.USUARIO)
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun comentarioVacioNoSeEnvia() = runTest(testDispatcher) {
        val vm = SocialViewModel(repo)
        vm.cargar("pub1", sesion.uid)
        advanceUntilIdle()
        vm.cambiarTexto("   ")

        vm.enviarComentario(sesion)
        advanceUntilIdle()

        assertEquals(0, repo.vecesComentado)
        assertEquals("Escribe un comentario antes de enviar", vm.estado.value.mensaje)
    }

    @Test
    fun comentarioLargoSeRecortaA500() = runTest(testDispatcher) {
        val vm = SocialViewModel(repo)
        val textoLargo = "A".repeat(600)
        vm.cambiarTexto(textoLargo)

        assertEquals(500, vm.estado.value.texto.length)
    }

    @Test
    fun dobleEnvioSoloLlamaUnaVez() = runTest(testDispatcher) {
        val vm = SocialViewModel(repo)
        vm.cargar("pub1", sesion.uid)
        advanceUntilIdle()
        vm.cambiarTexto("Comentario válido")

        vm.enviarComentario(sesion)
        vm.enviarComentario(sesion)
        advanceUntilIdle()

        assertEquals(1, repo.vecesComentado)
    }

    @Test
    fun responderARespuestaSeAsociaALaRaiz() = runTest(testDispatcher) {
        val raiz = Comentario("r1", "pub1", "u2", "u2@test.com", "Raíz", 1000L, null)
        val respuesta = Comentario("resp1", "pub1", "u3", "u3@test.com", "Respuesta", 1100L, "r1")
        repo.comentariosRetornados = listOf(raiz, respuesta)

        val vm = SocialViewModel(repo)
        vm.cargar("pub1", sesion.uid)
        advanceUntilIdle()
        vm.responderA(respuesta)
        vm.cambiarTexto("Sub-respuesta")

        vm.enviarComentario(sesion)
        advanceUntilIdle()

        assertEquals("r1", repo.ultimoRespuestaA)
    }

    @Test
    fun likeOptimistaYReversionAlFallar() = runTest(testDispatcher) {
        repo.resumenRetornado = ResumenSocial(totalLikes = 5, yoDiLike = false)
        repo.fallaLike = IOException("Error")

        val vm = SocialViewModel(repo)
        vm.cargar("pub1", sesion.uid)
        advanceUntilIdle()

        // Estado inicial
        assertEquals(5, vm.estado.value.resumen.totalLikes)
        assertFalse(vm.estado.value.resumen.yoDiLike)

        // Alternar like (optimista sube a 6 y true)
        vm.alternarLike("pub1", sesion.uid)
        assertTrue(vm.estado.value.resumen.yoDiLike)
        assertEquals(6, vm.estado.value.resumen.totalLikes)

        // Avanzar idle para que falle y revierta
        advanceUntilIdle()

        assertFalse(vm.estado.value.resumen.yoDiLike)
        assertEquals(5, vm.estado.value.resumen.totalLikes)
        assertEquals("No se pudo registrar. Revisa tu conexión", vm.estado.value.mensaje)
    }

    @Test
    fun resumenDeRepoSeReflejaEnEstado() = runTest(testDispatcher) {
        repo.resumenRetornado = ResumenSocial(promedio = 4.5, totalVotos = 10, totalLikes = 25, yoDiLike = true)

        val vm = SocialViewModel(repo)
        vm.cargar("pub1", sesion.uid)
        advanceUntilIdle()

        assertEquals(4.5, vm.estado.value.resumen.promedio, 0.01)
        assertEquals(10, vm.estado.value.resumen.totalVotos)
        assertEquals(25, vm.estado.value.resumen.totalLikes)
        assertTrue(vm.estado.value.resumen.yoDiLike)
    }
}
