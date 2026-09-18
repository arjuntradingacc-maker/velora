package com.velora.vault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.velora.vault.data.local.entity.BreachRecordEntity
import com.velora.vault.data.local.entity.DeviceSessionEntity
import com.velora.vault.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE expiresAt IS NOT NULL AND expiresAt > 0 ORDER BY expiresAt ASC")
    fun observeExpiring(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DocumentEntity)

    @Delete
    suspend fun delete(entity: DocumentEntity)
}

@Dao
interface BreachRecordDao {
    @Query("SELECT * FROM breach_records")
    fun observeAll(): Flow<List<BreachRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BreachRecordEntity)

    @Query("DELETE FROM breach_records WHERE loginId = :loginId")
    suspend fun deleteForLogin(loginId: String)
}

@Dao
interface DeviceSessionDao {
    @Query("SELECT * FROM device_sessions ORDER BY isCurrentDevice DESC, lastSyncAt DESC")
    fun observeAll(): Flow<List<DeviceSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DeviceSessionEntity)

    @Update
    suspend fun update(entity: DeviceSessionEntity)

    @Query("DELETE FROM device_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM device_sessions WHERE isCurrentDevice = 0")
    suspend fun deleteAllOtherDevices()
}
