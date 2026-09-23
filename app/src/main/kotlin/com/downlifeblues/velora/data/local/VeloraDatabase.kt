package com.downlifeblues.velora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.downlifeblues.velora.data.local.dao.ApiKeyDao
import com.downlifeblues.velora.data.local.dao.BreachRecordDao
import com.downlifeblues.velora.data.local.dao.CustomItemDao
import com.downlifeblues.velora.data.local.dao.DeviceSessionDao
import com.downlifeblues.velora.data.local.dao.DocumentDao
import com.downlifeblues.velora.data.local.dao.IdentityDao
import com.downlifeblues.velora.data.local.dao.LoginDao
import com.downlifeblues.velora.data.local.dao.OtpDao
import com.downlifeblues.velora.data.local.dao.PasskeyDao
import com.downlifeblues.velora.data.local.dao.PaymentCardDao
import com.downlifeblues.velora.data.local.dao.RecoveryCodeDao
import com.downlifeblues.velora.data.local.dao.SecureNoteDao
import com.downlifeblues.velora.data.local.dao.WifiDao
import com.downlifeblues.velora.data.local.entity.ApiKeyEntity
import com.downlifeblues.velora.data.local.entity.BreachRecordEntity
import com.downlifeblues.velora.data.local.entity.CustomItemEntity
import com.downlifeblues.velora.data.local.entity.DeviceSessionEntity
import com.downlifeblues.velora.data.local.entity.DocumentEntity
import com.downlifeblues.velora.data.local.entity.IdentityEntity
import com.downlifeblues.velora.data.local.entity.LoginEntity
import com.downlifeblues.velora.data.local.entity.OtpEntity
import com.downlifeblues.velora.data.local.entity.PasskeyEntity
import com.downlifeblues.velora.data.local.entity.PaymentCardEntity
import com.downlifeblues.velora.data.local.entity.RecoveryCodeEntity
import com.downlifeblues.velora.data.local.entity.SecureNoteEntity
import com.downlifeblues.velora.data.local.entity.WifiEntity

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
