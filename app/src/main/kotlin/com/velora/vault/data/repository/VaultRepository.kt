package com.velora.vault.data.repository

import com.velora.vault.data.local.VeloraDatabaseProvider
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
import com.velora.vault.data.model.VaultCategory
import com.velora.vault.data.model.VaultItemSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single point of access to every vault category. Screens ask this
 * repository rather than reaching for DAOs directly, and get an empty
 * result (never a crash) whenever the vault happens to be locked.
 */
@Singleton
class VaultRepository @Inject constructor(
    private val dbProvider: VeloraDatabaseProvider,
) {
    private fun dateLabel(millis: Long): String =
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

    // --- Logins ---------------------------------------------------------

    fun observeLogins(): Flow<List<LoginEntity>> = dbFlow { it.loginDao().observeAll() }
    fun observeRecentLogins(limit: Int = 8): Flow<List<LoginEntity>> = dbFlow { it.loginDao().observeRecent(limit) }
    fun observeLogin(id: String): Flow<LoginEntity?> =
        dbProvider.database.flatMapLatest { db -> if (db == null) flowOf(null) else db.loginDao().observeById(id) }
    suspend fun getLogin(id: String): LoginEntity? = dbProvider.requireDatabase().loginDao().getById(id)
    suspend fun saveLogin(entity: LoginEntity) = dbProvider.requireDatabase().loginDao().upsert(entity)
    suspend fun deleteLogin(entity: LoginEntity) = dbProvider.requireDatabase().loginDao().delete(entity)
    suspend fun markLoginUsed(id: String) = dbProvider.requireDatabase().loginDao().markUsed(id, System.currentTimeMillis())
    suspend fun setLoginFavorite(id: String, favorite: Boolean) =
        dbProvider.requireDatabase().loginDao().setFavorite(id, favorite)
    suspend fun allLoginsOnce(): List<LoginEntity> = dbProvider.requireDatabase().loginDao().getAllOnce()

    // --- Passkeys ---------------------------------------------------------
    fun observePasskeys(): Flow<List<PasskeyEntity>> = dbFlow { it.passkeyDao().observeAll() }
    suspend fun savePasskey(entity: PasskeyEntity) = dbProvider.requireDatabase().passkeyDao().upsert(entity)
    suspend fun deletePasskey(entity: PasskeyEntity) = dbProvider.requireDatabase().passkeyDao().delete(entity)

    // --- Secure notes -------------------------------------------------------
    fun observeNotes(): Flow<List<SecureNoteEntity>> = dbFlow { it.secureNoteDao().observeAll() }
    suspend fun saveNote(entity: SecureNoteEntity) = dbProvider.requireDatabase().secureNoteDao().upsert(entity)
    suspend fun deleteNote(entity: SecureNoteEntity) = dbProvider.requireDatabase().secureNoteDao().delete(entity)

    // --- Payment cards -------------------------------------------------------
    fun observeCards(): Flow<List<PaymentCardEntity>> = dbFlow { it.paymentCardDao().observeAll() }
    suspend fun saveCard(entity: PaymentCardEntity) = dbProvider.requireDatabase().paymentCardDao().upsert(entity)
    suspend fun deleteCard(entity: PaymentCardEntity) = dbProvider.requireDatabase().paymentCardDao().delete(entity)

    // --- Identities -------------------------------------------------------
    fun observeIdentities(): Flow<List<IdentityEntity>> = dbFlow { it.identityDao().observeAll() }
    suspend fun saveIdentity(entity: IdentityEntity) = dbProvider.requireDatabase().identityDao().upsert(entity)
    suspend fun deleteIdentity(entity: IdentityEntity) = dbProvider.requireDatabase().identityDao().delete(entity)

    // --- Wi-Fi -------------------------------------------------------
    fun observeWifi(): Flow<List<WifiEntity>> = dbFlow { it.wifiDao().observeAll() }
    suspend fun saveWifi(entity: WifiEntity) = dbProvider.requireDatabase().wifiDao().upsert(entity)
    suspend fun deleteWifi(entity: WifiEntity) = dbProvider.requireDatabase().wifiDao().delete(entity)

    // --- OTP -------------------------------------------------------
    fun observeOtp(): Flow<List<OtpEntity>> = dbFlow { it.otpDao().observeAll() }
    suspend fun saveOtp(entity: OtpEntity) = dbProvider.requireDatabase().otpDao().upsert(entity)
    suspend fun deleteOtp(entity: OtpEntity) = dbProvider.requireDatabase().otpDao().delete(entity)

    // --- Documents -------------------------------------------------------
    fun observeDocuments(): Flow<List<DocumentEntity>> = dbFlow { it.documentDao().observeAll() }
    fun observeExpiringDocuments(): Flow<List<DocumentEntity>> = dbFlow { it.documentDao().observeExpiring() }
    suspend fun saveDocument(entity: DocumentEntity) = dbProvider.requireDatabase().documentDao().upsert(entity)
    suspend fun deleteDocument(entity: DocumentEntity) = dbProvider.requireDatabase().documentDao().delete(entity)

    // --- API keys -------------------------------------------------------
    fun observeApiKeys(): Flow<List<ApiKeyEntity>> = dbFlow { it.apiKeyDao().observeAll() }
    suspend fun saveApiKey(entity: ApiKeyEntity) = dbProvider.requireDatabase().apiKeyDao().upsert(entity)
    suspend fun deleteApiKey(entity: ApiKeyEntity) = dbProvider.requireDatabase().apiKeyDao().delete(entity)

    // --- Recovery codes -------------------------------------------------------
    fun observeRecoveryCodes(): Flow<List<RecoveryCodeEntity>> = dbFlow { it.recoveryCodeDao().observeAll() }
    suspend fun saveRecoveryCodes(entity: RecoveryCodeEntity) =
        dbProvider.requireDatabase().recoveryCodeDao().upsert(entity)
    suspend fun deleteRecoveryCodes(entity: RecoveryCodeEntity) =
        dbProvider.requireDatabase().recoveryCodeDao().delete(entity)

    // --- Custom items -------------------------------------------------------
    fun observeCustomItems(): Flow<List<CustomItemEntity>> = dbFlow { it.customItemDao().observeAll() }
    suspend fun saveCustomItem(entity: CustomItemEntity) = dbProvider.requireDatabase().customItemDao().upsert(entity)
    suspend fun deleteCustomItem(entity: CustomItemEntity) = dbProvider.requireDatabase().customItemDao().delete(entity)

    // --- Security / breach records -------------------------------------------------------
    fun observeBreachRecords(): Flow<List<BreachRecordEntity>> = dbFlow { it.breachRecordDao().observeAll() }
    suspend fun saveBreachRecord(entity: BreachRecordEntity) = dbProvider.requireDatabase().breachRecordDao().upsert(entity)

    // --- Devices / sync bookkeeping -------------------------------------------------------
    fun observeDeviceSessions(): Flow<List<DeviceSessionEntity>> = dbFlow { it.deviceSessionDao().observeAll() }
    suspend fun saveDeviceSession(entity: DeviceSessionEntity) = dbProvider.requireDatabase().deviceSessionDao().upsert(entity)
    suspend fun markDeviceSynced(id: String, timestamp: Long) {
        val dao = dbProvider.requireDatabase().deviceSessionDao()
        // Re-upsert via a fresh read/modify since Room's generated DAO here has no partial-update query.
        val current = dao.observeAll().first().firstOrNull { it.id == id }
        current?.let { dao.update(it.copy(lastSyncAt = timestamp)) }
    }
    suspend fun removeAllOtherDeviceSessions() = dbProvider.requireDatabase().deviceSessionDao().deleteAllOtherDevices()

    // --- Cross-category views -------------------------------------------------------

    fun observeAllSummaries(): Flow<List<VaultItemSummary>> = combine(
        observeLogins(), observePasskeys(), observeNotes(), observeCards(),
        observeIdentities(), observeWifi(), observeOtp(), observeDocuments(),
        observeApiKeys(), observeRecoveryCodes(), observeCustomItems(),
    ) { array: Array<List<*>> ->
        @Suppress("UNCHECKED_CAST")
        buildList {
            (array[0] as List<LoginEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.LOGIN, it.name, it.username ?: it.websiteUrl.orEmpty(), it.iconUrl, it.isFavorite, it.updatedAt))
            }
            (array[1] as List<PasskeyEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.PASSKEY, it.relyingPartyName, it.username, it.iconUrl, it.isFavorite, it.lastUsedAt ?: it.createdAt))
            }
            (array[2] as List<SecureNoteEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.SECURE_NOTE, it.title, "Updated ${dateLabel(it.updatedAt)}", null, it.isFavorite, it.updatedAt))
            }
            (array[3] as List<PaymentCardEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.PAYMENT_CARD, it.nickname, "•••• ${it.lastFour}", null, it.isFavorite, it.updatedAt))
            }
            (array[4] as List<IdentityEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.IDENTITY, it.label, it.fullName, null, it.isFavorite, it.updatedAt))
            }
            (array[5] as List<WifiEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.WIFI, it.ssid, it.security, null, it.isFavorite, it.updatedAt))
            }
            (array[6] as List<OtpEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.OTP, it.issuer, it.accountName, it.iconUrl, it.isFavorite, it.createdAt))
            }
            (array[7] as List<DocumentEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.DOCUMENT, it.name, it.type.name, null, it.isFavorite, it.updatedAt))
            }
            (array[8] as List<ApiKeyEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.API_KEY, it.serviceName, it.keyLabel, null, it.isFavorite, it.updatedAt))
            }
            (array[9] as List<RecoveryCodeEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.RECOVERY_CODE, it.serviceName, "${it.codes.size} codes", null, it.isFavorite, it.createdAt))
            }
            (array[10] as List<CustomItemEntity>).forEach {
                add(VaultItemSummary(it.id, VaultCategory.CUSTOM, it.title, it.categoryLabel, null, it.isFavorite, it.updatedAt))
            }
        }.sortedByDescending { it.updatedAt }
    }

    fun observeFavorites(): Flow<List<VaultItemSummary>> = observeAllSummaries().map { list -> list.filter { it.isFavorite } }

    fun observeRecentSummaries(limit: Int = 6): Flow<List<VaultItemSummary>> =
        observeAllSummaries().map { it.take(limit) }

    suspend fun search(query: String): List<VaultItemSummary> {
        if (query.isBlank()) return emptyList()
        val lower = query.lowercase()
        val db = dbProvider.requireDatabase()
        return buildList {
            db.loginDao().search(query).forEach {
                add(VaultItemSummary(it.id, VaultCategory.LOGIN, it.name, it.username ?: it.websiteUrl.orEmpty(), it.iconUrl, it.isFavorite, it.updatedAt))
            }
        }.filter {
            it.title.lowercase().contains(lower) || it.subtitle.lowercase().contains(lower)
        }
    }

    private fun <T> dbFlow(block: (com.velora.vault.data.local.VeloraDatabase) -> Flow<List<T>>): Flow<List<T>> =
        dbProvider.database.flatMapLatest { db -> if (db == null) flowOf(emptyList()) else block(db) }
}
