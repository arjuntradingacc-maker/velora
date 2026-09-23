package com.downlifeblues.velora.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.downlifeblues.velora.data.local.entity.LoginEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoginDao {
    @Query("SELECT * FROM logins ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LoginEntity>>

    @Query("SELECT * FROM logins ORDER BY lastUsedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 8): Flow<List<LoginEntity>>

    @Query("SELECT * FROM logins WHERE isFavorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<LoginEntity>>

    @Query("SELECT * FROM logins WHERE id = :id")
    suspend fun getById(id: String): LoginEntity?

    @Query("SELECT * FROM logins WHERE id = :id")
    fun observeById(id: String): Flow<LoginEntity?>

    @Query(
        """SELECT * FROM logins WHERE
        name LIKE '%' || :query || '%' OR
        username LIKE '%' || :query || '%' OR
        websiteUrl LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC""",
    )
    suspend fun search(query: String): List<LoginEntity>

    @Query("SELECT * FROM logins")
    suspend fun getAllOnce(): List<LoginEntity>

    @Query("SELECT COUNT(*) FROM logins")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LoginEntity)

    @Update
    suspend fun update(entity: LoginEntity)

    @Delete
    suspend fun delete(entity: LoginEntity)

    @Query("DELETE FROM logins WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE logins SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun markUsed(id: String, timestamp: Long)

    @Query("UPDATE logins SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)
}
