package com.lajara.blopps.data.repository

import com.lajara.blopps.domain.model.PeliculaBusqueda

interface PeliculaRepository {
    val disponible: Boolean
    suspend fun buscar(texto: String): Result<List<PeliculaBusqueda>>
}
