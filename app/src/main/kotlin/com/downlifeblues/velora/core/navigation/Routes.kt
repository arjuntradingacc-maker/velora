package com.downlifeblues.velora.core.navigation

/** Every navigable destination in the app. Grouped by flow, not by screen file. */
object Routes {
    // Onboarding
    const val ONBOARDING = "onboarding"

    // Auth
    const val WELCOME = "auth/welcome"
    const val SIGN_UP = "auth/sign-up"
    const val CREATE_MASTER_PASSWORD = "auth/master-password"
    const val BIOMETRIC_SETUP = "auth/biometric-setup"
    const val RECOVERY_CODE_REVEAL = "auth/recovery-code"
    const val UNLOCK = "auth/unlock"
    const val FORGOT_PASSWORD = "auth/forgot-password"

    // Bottom-nav top-level destinations
    const val VAULT_HOME = "vault/home"
    const val VAULT_LIST = "vault/list"
    const val SECURITY_CENTER = "security/center"
    const val GENERATOR = "generator"
    const val SETTINGS = "settings"

    // Vault category browsing
    const val VAULT_CATEGORY = "vault/category/{category}"
    fun vaultCategory(category: String) = "vault/category/$category"

    const val SEARCH = "search"

    // Logins
    const val PASSWORD_DETAIL = "item/password/{id}"
    fun passwordDetail(id: String) = "item/password/$id"
    const val PASSWORD_ADD = "item/password/add"
    const val PASSWORD_EDIT = "item/password/edit/{id}"
    fun passwordEdit(id: String) = "item/password/edit/$id"

    // Passkeys
    const val PASSKEYS = "passkeys"
    const val PASSKEY_DETAIL = "passkeys/{id}"
    fun passkeyDetail(id: String) = "passkeys/$id"

    // Secure notes
    const val NOTE_DETAIL = "item/note/{id}"
    fun noteDetail(id: String) = "item/note/$id"
    const val NOTE_ADD = "item/note/add"
    const val NOTE_EDIT = "item/note/edit/{id}"
    fun noteEdit(id: String) = "item/note/edit/$id"

    // Payment cards
    const val CARD_DETAIL = "item/card/{id}"
    fun cardDetail(id: String) = "item/card/$id"
    const val CARD_ADD = "item/card/add"
    const val CARD_EDIT = "item/card/edit/{id}"
    fun cardEdit(id: String) = "item/card/edit/$id"

    // Identity
    const val IDENTITY_DETAIL = "item/identity/{id}"
    fun identityDetail(id: String) = "item/identity/$id"
    const val IDENTITY_ADD = "item/identity/add"
    const val IDENTITY_EDIT = "item/identity/edit/{id}"
    fun identityEdit(id: String) = "item/identity/edit/$id"

    // Wi-Fi
    const val WIFI_DETAIL = "item/wifi/{id}"
    fun wifiDetail(id: String) = "item/wifi/$id"
    const val WIFI_ADD = "item/wifi/add"

    // OTP
    const val OTP_LIST = "item/otp"
    const val OTP_ADD = "item/otp/add"

    // Documents
    const val DOCUMENTS = "item/document"
    const val DOCUMENT_DETAIL = "item/document/{id}"
    fun documentDetail(id: String) = "item/document/$id"

    // API keys
    const val API_KEY_DETAIL = "item/api-key/{id}"
    fun apiKeyDetail(id: String) = "item/api-key/$id"
    const val API_KEY_ADD = "item/api-key/add"

    // Recovery codes
    const val RECOVERY_CODES_DETAIL = "item/recovery-codes/{id}"
    fun recoveryCodesDetail(id: String) = "item/recovery-codes/$id"
    const val RECOVERY_CODES_ADD = "item/recovery-codes/add"

    // Custom items
    const val CUSTOM_ITEM_DETAIL = "item/custom/{id}"
    fun customItemDetail(id: String) = "item/custom/$id"
    const val CUSTOM_ITEM_ADD = "item/custom/add"

    // Security
    const val BREACH_MONITORING = "security/breach"
    const val SECURITY_ISSUE_DETAIL = "security/issue/{type}"
    fun securityIssueDetail(type: String) = "security/issue/$type"

    // Settings
    const val SETTINGS_SECURITY = "settings/security"
    const val SETTINGS_VAULT = "settings/vault"
    const val SETTINGS_SYNC = "settings/sync"
    const val SETTINGS_APPEARANCE = "settings/appearance"
    const val SETTINGS_NOTIFICATIONS = "settings/notifications"
    const val SETTINGS_PRIVACY = "settings/privacy"
}
