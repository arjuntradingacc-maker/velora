package com.velora.vault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.velora.vault.data.local.dao.ApiKeyDao
import com.velora.vault.data.local.dao.BreachRecordDao
import com.velora.vault.data.local.dao.CustomItemDao
import com.velora.vault.data.local.dao.DeviceSessionDao
import com.velora.vault.data.local.dao.DocumentDao
import com.velora.vault.data.local.dao.IdentityDao
import com.velora.vault.data.local.dao.LoginDao
import com.velora.vault.data.local.dao.OtpDao
import com.velora.vault.data.local.dao.PasskeyDao
import com.velora.vault.data.local.dao.PaymentCardDao
import com.velora.vault.data.local.dao.RecoveryCodeDao
import com.velora.vault.data.local.dao.SecureNoteDao
import com.velora.vault.data.local.dao.WifiDao
import com.velora.vault.data.local.entity.ApiKeyEntity
import com.velora.vault.data.local.entity.BreachRecordEntity
import com.velora.vault.data.local.entity.CustomItemEntity
import com.velora.vault.data.local.entity.DeviceSessionEntity
import com.velora.vault.data.local.entity.DocumentEntity
import com.velora.vault.data.local.entity.IdentityEntity
import com.velora.vault.data.local.entity.LoginEntity
import com.velora.vault.data.local.entity.OtpEntity
import com.velora.vault.data.local.entity.PasskeyEntity
import com.velora.vault.data.local.entity.PaymentCardEntity
import com.velora.vault.data.local.entity.RecoveryCodeEntity
import com.velora.vault.data.local.entity.SecureNoteEntity
import com.velora.vault.data.local.entity.WifiEntity

@Database(
    entities = [
        LoginEntity::class,
        PasskeyEntity::class,
        WifiEntity::class,
        OtpEntity::class,
        ApiKeyEntity::class,
        RecoveryCodeEntity::class,
        SecureNoteEntity::class,
        CustomItemEntity::class,
        PaymentCardEntity::class,
        IdentityEntity::class,
        DocumentEntity::class,
        BreachRecordEntity::class,
        DeviceSessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class VeloraDatabase : RoomDatabase() {
    abstract fun loginDao(): LoginDao
    abstract fun passkeyDao(): PasskeyDao
    abstract fun wifiDao(): WifiDao
    abstract fun otpDao(): OtpDao
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun recoveryCodeDao(): RecoveryCodeDao
    abstract fun secureNoteDao(): SecureNoteDao
    abstract fun customItemDao(): CustomItemDao
    abstract fun paymentCardDao(): PaymentCardDao
    abstract fun identityDao(): IdentityDao
    abstract fun documentDao(): DocumentDao
    abstract fun breachRecordDao(): BreachRecordDao
    abstract fun deviceSessionDao(): DeviceSessionDao

    companion object {
        const val FILE_NAME = "velora_vault.db"
    }
}
