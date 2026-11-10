package com.lajara.blopps.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PublicacionDto(
    val userId: Int,
    val id: Int,
    val title: String = "",
    val body: String = ""
)
