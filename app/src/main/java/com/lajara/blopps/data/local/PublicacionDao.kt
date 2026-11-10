package com.lajara.blopps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class ConteoFavorito(
    val publicacionId: Int,
    val total: Int
)

@Dao
interface PublicacionDao {

    @Query("SELECT * FROM publicaciones WHERE comunidad = :comunidad ORDER BY id")
    fun porComunidad(comunidad: String): Flow<List<PublicacionEntity>>

    @Query(
        "SELECT p.* FROM publicaciones p " +
                "INNER JOIN favoritos f ON f.publicacionId = p.id " +
                "WHERE f.uid = :uid ORDER BY p.id"
    )
    fun favoritas(uid: String): Flow<List<PublicacionEntity>>

    @Query("SELECT publicacionId FROM favoritos WHERE uid = :uid")
    fun idsFavoritos(uid: String): Flow<List<Int>>

    @Query("SELECT * FROM publicaciones WHERE comunidad = :comunidad")
    suspend fun instantanea(comunidad: String): List<PublicacionEntity>

    @Query("SELECT * FROM publicaciones WHERE id = :id")
    suspend fun porId(id: Int): PublicacionEntity?

    @Query("SELECT COUNT(*) FROM publicaciones")
    suspend fun contar(): Int

    @Query(
        "SELECT COUNT(*) FROM favoritos f " +
                "INNER JOIN publicaciones p ON p.id = f.publicacionId"
    )
    suspend fun contarFavoritasTotales(): Int

    @Query("SELECT COUNT(*) FROM publicaciones WHERE comunidad = :comunidad")
    suspend fun contarPorComunidad(comunidad: String): Int

    @Query("SELECT COUNT(*) FROM favoritos WHERE uid = :uid AND publicacionId = :id")
    suspend fun contarFavorito(uid: String, id: Int): Int

    @Query(
        "SELECT f.publicacionId AS publicacionId, COUNT(*) AS total FROM favoritos f " +
                "INNER JOIN publicaciones p ON p.id = f.publicacionId " +
                "GROUP BY f.publicacionId ORDER BY total DESC, f.publicacionId LIMIT :limite"
    )
    suspend fun conteoFavoritos(limite: Int): List<ConteoFavorito>

    @Upsert
    suspend fun guardarTodas(items: List<PublicacionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarFavorito(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE uid = :uid AND publicacionId = :id")
    suspend fun quitarFavorito(uid: String, id: Int)

    @Query("DELETE FROM publicaciones")
    suspend fun borrarTodo()
}