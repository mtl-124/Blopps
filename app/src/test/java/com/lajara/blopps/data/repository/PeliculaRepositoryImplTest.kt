package com.lajara.blopps.data.repository

import com.lajara.blopps.data.remote.MovieApiService
import com.lajara.blopps.data.remote.dto.MovieDetailDto
import com.lajara.blopps.data.remote.dto.MovieDto
import com.lajara.blopps.data.remote.dto.MovieListResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

private class FakeMovieApiService(
    var resultsToReturn: List<MovieDto> = emptyList(),
    var errorToThrow: Throwable? = null
) : MovieApiService {
    var callCount = 0

    override suspend fun descubrir(generoId: Int, pagina: Int): MovieListResponseDto {
        return MovieListResponseDto()
    }

    override suspend fun detalle(id: Int, adicional: String): MovieDetailDto {
        return MovieDetailDto(id)
    }

    override suspend fun buscar(texto: String, pagina: Int): MovieListResponseDto {
        callCount++
        errorToThrow?.let { throw it }
        return MovieListResponseDto(results = resultsToReturn)
    }
}

class PeliculaRepositoryImplTest {

    @Test
    fun textoMenosDeDosCaracteresDevuelveVacioSinLlamarApi() = runTest {
        val api = FakeMovieApiService(resultsToReturn = listOf(MovieDto(id = 1, title = "Test")))
        val repo = PeliculaRepositoryImpl(api)

        val result = repo.buscar("a")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
        assertEquals(0, api.callCount)
    }

    @Test
    fun apiNulaDevuelveNoDisponibleYFalla() = runTest {
        val repo = PeliculaRepositoryImpl(null)

        assertFalse(repo.disponible)
        val result = repo.buscar("Matrix")

        assertTrue(result.isFailure)
        assertEquals("Búsqueda no disponible: falta la clave de TMDB", result.exceptionOrNull()?.message)
    }

    @Test
    fun quinceResultadosSeLimitanADiez() = runTest {
        val movies = (1..15).map { MovieDto(id = it, title = "Pelicula $it") }
        val api = FakeMovieApiService(resultsToReturn = movies)
        val repo = PeliculaRepositoryImpl(api)

        val result = repo.buscar("Pelicula")

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrThrow().size)
        assertEquals(1, api.callCount)
    }

    @Test
    fun posterNuloYReleaseDateVacioProducenNulos() = runTest {
        val movie = MovieDto(
            id = 1,
            title = "Matrix",
            posterPath = null,
            releaseDate = ""
        )
        val api = FakeMovieApiService(resultsToReturn = listOf(movie))
        val repo = PeliculaRepositoryImpl(api)

        val result = repo.buscar("Matrix")

        assertTrue(result.isSuccess)
        val pelicula = result.getOrThrow().first()
        assertNull(pelicula.posterUrl)
        assertNull(pelicula.anio)
    }

    @Test
    fun ioExceptionDevuelveFailure() = runTest {
        val ioEx = IOException("Error de red")
        val api = FakeMovieApiService(errorToThrow = ioEx)
        val repo = PeliculaRepositoryImpl(api)

        val result = repo.buscar("Matrix")

        assertTrue(result.isFailure)
        assertEquals(ioEx, result.exceptionOrNull())
    }

    @Test
    fun resultadosConTituloEnBlancoSeDescartan() = runTest {
        val movies = listOf(
            MovieDto(id = 1, title = "   "),
            MovieDto(id = 2, title = "Matrix"),
            MovieDto(id = 3, title = "")
        )
        val api = FakeMovieApiService(resultsToReturn = movies)
        val repo = PeliculaRepositoryImpl(api)

        val result = repo.buscar("Matrix")

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals("Matrix", list.first().titulo)
    }
}
