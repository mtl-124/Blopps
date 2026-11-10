package com.lajara.blopps.data.local

import androidx.room.Entity

@Entity(tableName = "favoritos", primaryKeys = ["uid", "publicacionId"])
data class FavoritoEntity(
    val uid: String,
    val publicacionId: Int
)