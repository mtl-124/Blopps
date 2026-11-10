package com.lajara.blopps.data.remote

import com.lajara.blopps.data.remote.dto.PublicacionDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PublicacionApiService {

    @GET("posts")
    suspend fun listar(@Query("userId") usuarioId: Int): List<PublicacionDto>

    @GET("posts/{id}")
    suspend fun detalle(@Path("id") id: Int): PublicacionDto
}