package com.lajara.blopps.data.remote

import com.lajara.blopps.data.remote.dto.MovieDetailDto
import com.lajara.blopps.data.remote.dto.MovieListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {

    @GET("discover/movie")
    suspend fun descubrir(
        @Query("with_genres") generoId: Int,
        @Query("page") pagina: Int = 1
    ): MovieListResponseDto

    @GET("movie/{id}")
    suspend fun detalle(
        @Path("id") id: Int,
        @Query("append_to_response") adicional: String = "credits"
    ): MovieDetailDto

    @GET("search/movie")
    suspend fun buscar(
        @Query("query") texto: String,
        @Query("page") pagina: Int = 1
    ): MovieListResponseDto
}