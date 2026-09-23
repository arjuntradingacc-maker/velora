package com.downlifeblues.velora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BreachStatus { SAFE, ATTENTION, CRITICAL }

/**
 * Result of the last privacy-preserving breach check for one login. Checks
 * run via a k-anonymity hash-prefix lookup (see BreachCheckRepository) —
 * this table only ever stores the *result*, never sends the credential
 * itself anywhere.
 */
@Entity(tableName = "breach_records")
data class BreachRecordEntity(
    @PrimaryKey val loginId: String,
    val status: BreachStatus,
    val serviceName: String,
    val discoveredAt: Long,
    val lastCheckedAt: Long,
    val recommendedAction: String,
)

@Entity(tableName = "device_sessions")
data class DeviceSessionEntity(
    @PrimaryKey val id: String,
    val deviceName: String,
    val platform: String,
    val lastSyncAt: Long?,
    val isCurrentDevice: Boolean,
    val addedAt: Long,
)
