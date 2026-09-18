package com.velora.vault.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.velora.vault.data.local.entity.ApiKeyEntity
import com.velora.vault.data.local.entity.OtpEntity
import com.velora.vault.data.local.entity.PasskeyEntity
import com.velora.vault.data.local.entity.RecoveryCodeEntity
import com.velora.vault.data.local.entity.WifiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasskeyDao {
    @Query("SELECT * FROM passkeys ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PasskeyEntity>>

    @Query("SELECT * FROM passkeys WHERE id = :id")
    suspend fun getById(id: String): PasskeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PasskeyEntity)

    @Update
    suspend fun update(entity: PasskeyEntity)

    @Delete
    suspend fun delete(entity: PasskeyEntity)
}

@Dao
interface WifiDao {
    @Query("SELECT * FROM wifi_networks ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WifiEntity>>

    @Query("SELECT * FROM wifi_networks WHERE id = :id")
    suspend fun getById(id: String): WifiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WifiEntity)

    @Delete
    suspend fun delete(entity: WifiEntity)
}

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_accounts ORDER BY issuer ASC")
    fun observeAll(): Flow<List<OtpEntity>>

    @Query("SELECT * FROM otp_accounts WHERE id = :id")
    suspend fun getById(id: String): OtpEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OtpEntity)

    @Delete
    suspend fun delete(entity: OtpEntity)
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ApiKeyEntity>>

    @Query("SELECT * FROM api_keys WHERE id = :id")
    suspend fun getById(id: String): ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ApiKeyEntity)

    @Delete
    suspend fun delete(entity: ApiKeyEntity)
}

@Dao
interface RecoveryCodeDao {
    @Query("SELECT * FROM recovery_codes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RecoveryCodeEntity>>

    @Query("SELECT * FROM recovery_codes WHERE id = :id")
    suspend fun getById(id: String): RecoveryCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecoveryCodeEntity)

    @Delete
    suspend fun delete(entity: RecoveryCodeEntity)
}
