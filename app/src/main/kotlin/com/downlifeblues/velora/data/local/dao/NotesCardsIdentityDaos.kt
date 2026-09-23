package com.downlifeblues.velora.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.downlifeblues.velora.data.local.entity.CustomItemEntity
import com.downlifeblues.velora.data.local.entity.IdentityEntity
import com.downlifeblues.velora.data.local.entity.PaymentCardEntity
import com.downlifeblues.velora.data.local.entity.SecureNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SecureNoteDao {
    @Query("SELECT * FROM secure_notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SecureNoteEntity>>

    @Query("SELECT * FROM secure_notes WHERE id = :id")
    suspend fun getById(id: String): SecureNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SecureNoteEntity)

    @Update
    suspend fun update(entity: SecureNoteEntity)

    @Delete
    suspend fun delete(entity: SecureNoteEntity)
}

@Dao
interface CustomItemDao {
    @Query("SELECT * FROM custom_items ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CustomItemEntity>>

    @Query("SELECT * FROM custom_items WHERE id = :id")
    suspend fun getById(id: String): CustomItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CustomItemEntity)

    @Delete
    suspend fun delete(entity: CustomItemEntity)
}

@Dao
interface PaymentCardDao {
    @Query("SELECT * FROM payment_cards ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PaymentCardEntity>>

    @Query("SELECT * FROM payment_cards WHERE id = :id")
    suspend fun getById(id: String): PaymentCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PaymentCardEntity)

    @Update
    suspend fun update(entity: PaymentCardEntity)

    @Delete
    suspend fun delete(entity: PaymentCardEntity)
}

@Dao
interface IdentityDao {
    @Query("SELECT * FROM identities ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<IdentityEntity>>

    @Query("SELECT * FROM identities WHERE id = :id")
    suspend fun getById(id: String): IdentityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: IdentityEntity)

    @Update
    suspend fun update(entity: IdentityEntity)

    @Delete
    suspend fun delete(entity: IdentityEntity)
}
