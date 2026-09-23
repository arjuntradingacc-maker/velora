package com.downlifeblues.velora.data.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.downlifeblues.velora.core.design.icons.VeloraIcons

enum class VaultCategory(val displayName: String, val icon: ImageVector) {
    LOGIN("Logins", VeloraIcons.Logins),
    PASSKEY("Passkeys", VeloraIcons.Passkeys),
    SECURE_NOTE("Secure Notes", VeloraIcons.SecureNotes),
    PAYMENT_CARD("Payment Cards", VeloraIcons.PaymentCards),
    IDENTITY("Personal Information", VeloraIcons.PersonalInfo),
    WIFI("Wi-Fi", VeloraIcons.Wifi),
    OTP("OTP / Authenticator", VeloraIcons.Otp),
    DOCUMENT("Documents", VeloraIcons.Documents),
    API_KEY("API Keys", VeloraIcons.ApiKeys),
    RECOVERY_CODE("Recovery Codes", VeloraIcons.RecoveryCodes),
    CUSTOM("Custom Items", VeloraIcons.CustomItems),
}

/** A single arbitrary user-defined field attached to any vault item (add/edit screens). */
@kotlinx.serialization.Serializable
data class CustomField(
    val label: String,
    val value: String,
    val isSensitive: Boolean = false,
)
