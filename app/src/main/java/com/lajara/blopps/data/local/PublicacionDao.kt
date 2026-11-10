package com.lajara.blopps.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {

    @Query("SELECT * FROM publicaciones WHERE comunidad = :comunidad ORDER BY id")
    fun porComunidad(comunidad: String): Flow<List<PublicacionEntity>>

    @Query("SELECT * FROM publicaciones WHERE esFavorita = 1 ORDER BY id")
    fun favoritas(): Flow<List<PublicacionEntity>>

    @Query("SELECT * FROM publicaciones WHERE comunidad = :comunidad")
    suspend fun instantanea(comunidad: String): List<PublicacionEntity>

    @Query("SELECT * FROM publicaciones WHERE id = :id")
    suspend fun porId(id: Int): PublicacionEntity?

    @Query("SELECT COUNT(*) FROM publicaciones")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM publicaciones WHERE esFavorita = 1")
    suspend fun contarFavoritas(): Int

    @Query("SELECT COUNT(*) FROM publicaciones WHERE comunidad = :comunidad")
    suspend fun contarPorComunidad(comunidad: String): Int

    @Upsert
    suspend fun guardarTodas(items: List<PublicacionEntity>)

    @Query("UPDATE publicaciones SET esFavorita = NOT esFavorita WHERE id = :id")
    suspend fun alternarFavorita(id: Int)

    @Query("DELETE FROM publicaciones")
    suspend fun borrarTodo()
}