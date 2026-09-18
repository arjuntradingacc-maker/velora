package com.velora.vault.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CardNetwork { VISA, MASTERCARD, AMEX, DISCOVER, OTHER }

@Entity(tableName = "payment_cards")
data class PaymentCardEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val cardholderName: String,
    val network: CardNetwork = CardNetwork.OTHER,
    val fullNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
    val pin: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
) {
    val lastFour: String get() = fullNumber.takeLast(4)
}

@Entity(tableName = "identities")
data class IdentityEntity(
    @PrimaryKey val id: String,
    val label: String,
    val fullName: String,
    val email: String? = null,
    val phone: String? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val dateOfBirth: String? = null,
    val passportNumber: String? = null,
    val driverLicenseNumber: String? = null,
    val customFields: List<com.velora.vault.data.model.CustomField> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
