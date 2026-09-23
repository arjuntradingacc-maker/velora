package com.downlifeblues.velora.core.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.downlifeblues.velora.feature.auth.BiometricSetupScreen
import com.downlifeblues.velora.feature.auth.CreateMasterPasswordScreen
import com.downlifeblues.velora.feature.auth.ForgotPasswordScreen
import com.downlifeblues.velora.feature.auth.RecoveryCodeRevealScreen
import com.downlifeblues.velora.feature.auth.SignUpScreen
import com.downlifeblues.velora.feature.auth.UnlockScreen
import com.downlifeblues.velora.feature.cards.AddEditCardScreen
import com.downlifeblues.velora.feature.cards.CardDetailScreen
import com.downlifeblues.velora.feature.documents.DocumentDetailScreen
import com.downlifeblues.velora.feature.documents.DocumentsScreen
import com.downlifeblues.velora.feature.generator.GeneratorScreen
import com.downlifeblues.velora.feature.home.VaultHomeScreen
import com.downlifeblues.velora.feature.identity.AddEditIdentityScreen
import com.downlifeblues.velora.feature.identity.IdentityDetailScreen
import com.downlifeblues.velora.feature.itemdetail.AddEditPasswordScreen
import com.downlifeblues.velora.feature.itemdetail.PasswordDetailScreen
import com.downlifeblues.velora.feature.notes.AddEditNoteScreen
import com.downlifeblues.velora.feature.notes.NoteDetailScreen
import com.downlifeblues.velora.feature.onboarding.OnboardingScreen
import com.downlifeblues.velora.feature.otp.OtpListScreen
import com.downlifeblues.velora.feature.passkeys.PasskeyDetailScreen
import com.downlifeblues.velora.feature.passkeys.PasskeysScreen
import com.downlifeblues.velora.feature.security.BreachMonitoringScreen
import com.downlifeblues.velora.feature.security.SecurityCenterScreen
import com.downlifeblues.velora.feature.security.SecurityIssueDetailScreen
import com.downlifeblues.velora.feature.settings.SettingsAppearanceScreen
import com.downlifeblues.velora.feature.settings.SettingsNotificationsScreen
import com.downlifeblues.velora.feature.settings.SettingsPrivacyScreen
import com.downlifeblues.velora.feature.settings.SettingsScreen
import com.downlifeblues.velora.feature.settings.SettingsSecurityScreen
import com.downlifeblues.velora.feature.settings.SettingsSyncScreen
import com.downlifeblues.velora.feature.settings.SettingsVaultScreen
import com.downlifeblues.velora.data.model.VaultCategory
import com.downlifeblues.velora.feature.vault.VaultCategoryScreen
import com.downlifeblues.velora.feature.vault.apikey.AddEditApiKeyScreen
import com.downlifeblues.velora.feature.vault.custom.AddEditCustomItemScreen
import com.downlifeblues.velora.feature.vault.recovery.AddEditRecoveryCodesScreen
import com.downlifeblues.velora.feature.vault.wifi.AddEditWifiScreen
import com.downlifeblues.velora.feature.search.SearchScreen

@Composable
fun VeloraNavHost(navController: NavHostController, launchAction: String?) {
    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING,
        enterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) + slideInHorizontally(initialOffsetX = { it / 8 }) },
        exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) },
        popExitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) + slideOutHorizontally(targetOffsetX = { it / 8 }) },
    ) {
        // --- Onboarding & auth -------------------------------------------------------
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onDone = { navController.navigate(Routes.SIGN_UP) { popUpTo(Routes.ONBOARDING) { inclusive = true } } },
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(onAccountCreated = { navController.navigate(Routes.CREATE_MASTER_PASSWORD) })
        }
        composable(Routes.CREATE_MASTER_PASSWORD) {
            CreateMasterPasswordScreen(onMasterPasswordCreated = { navController.navigate(Routes.BIOMETRIC_SETUP) })
        }
        composable(Routes.BIOMETRIC_SETUP) {
            BiometricSetupScreen(
                onDone = {
                    navController.navigate(Routes.RECOVERY_CODE_REVEAL) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.RECOVERY_CODE_REVEAL) {
            RecoveryCodeRevealScreen(onDone = { navController.navigate(Routes.VAULT_HOME) { popUpTo(0) } })
        }
        composable(Routes.UNLOCK) {
            UnlockScreen(
                onUnlocked = { navController.navigate(Routes.VAULT_HOME) { popUpTo(0) } },
                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onRecovered = { navController.navigate(Routes.VAULT_HOME) { popUpTo(0) } },
                onBack = { navController.popBackStack() },
            )
        }

        // --- Bottom-nav top-level destinations -------------------------------------------------------
        composable(Routes.VAULT_HOME) {
            VaultHomeScreen(
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenCategory = { category -> navController.navigateToCategory(category) },
                onOpenItem = { category, id -> navController.navigateToItem(category, id) },
                onOpenSecurityCenter = { navController.navigate(Routes.SECURITY_CENTER) },
                onQuickAdd = { category -> navController.navigateToAdd(category) },
            )
        }
        composable(Routes.SECURITY_CENTER) {
            SecurityCenterScreen(
                onOpenIssue = { type -> navController.navigate(Routes.securityIssueDetail(type)) },
                onOpenBreachMonitoring = { navController.navigate(Routes.BREACH_MONITORING) },
                onOpenLogin = { id -> navController.navigate(Routes.passwordDetail(id)) },
            )
        }
        composable(Routes.GENERATOR) { GeneratorScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onOpenSecurity = { navController.navigate(Routes.SETTINGS_SECURITY) },
                onOpenVault = { navController.navigate(Routes.SETTINGS_VAULT) },
                onOpenSync = { navController.navigate(Routes.SETTINGS_SYNC) },
                onOpenAppearance = { navController.navigate(Routes.SETTINGS_APPEARANCE) },
                onOpenNotifications = { navController.navigate(Routes.SETTINGS_NOTIFICATIONS) },
                onOpenPrivacy = { navController.navigate(Routes.SETTINGS_PRIVACY) },
                onLoggedOut = { navController.navigate(Routes.ONBOARDING) { popUpTo(0) } },
            )
        }
        composable(Routes.SETTINGS_SECURITY) { SettingsSecurityScreen(onBack = navController::popBackStack) }
        composable(Routes.SETTINGS_VAULT) { SettingsVaultScreen(onBack = navController::popBackStack) }
        composable(Routes.SETTINGS_SYNC) { SettingsSyncScreen(onBack = navController::popBackStack) }
        composable(Routes.SETTINGS_APPEARANCE) { SettingsAppearanceScreen(onBack = navController::popBackStack) }
        composable(Routes.SETTINGS_NOTIFICATIONS) { SettingsNotificationsScreen(onBack = navController::popBackStack) }
        composable(Routes.SETTINGS_PRIVACY) {
            SettingsPrivacyScreen(
                onBack = navController::popBackStack,
                onAccountDeleted = { navController.navigate(Routes.ONBOARDING) { popUpTo(0) } },
            )
        }

        // --- Search -------------------------------------------------------
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = navController::popBackStack,
                onOpenItem = { category, id -> navController.navigateToItem(category, id) },
            )
        }

        // --- Vault category browsing -------------------------------------------------------
        composable(
            Routes.VAULT_CATEGORY,
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category").orEmpty()
            VaultCategoryScreen(
                categoryName = category,
                onBack = navController::popBackStack,
                onOpenItem = { id -> navController.navigateToItem(category, id) },
                onAddItem = { navController.navigateToAdd(category) },
            )
        }

        // --- Logins -------------------------------------------------------
        composable(
            Routes.PASSWORD_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            PasswordDetailScreen(
                loginId = id,
                onBack = navController::popBackStack,
                onEdit = { navController.navigate(Routes.passwordEdit(id)) },
                onDeleted = navController::popBackStack,
            )
        }
        composable(Routes.PASSWORD_ADD) {
            AddEditPasswordScreen(loginId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.PASSWORD_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditPasswordScreen(
                loginId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Passkeys -------------------------------------------------------
        composable(Routes.PASSKEYS) {
            PasskeysScreen(
                onBack = navController::popBackStack,
                onOpenPasskey = { id -> navController.navigate(Routes.passkeyDetail(id)) },
            )
        }
        composable(
            Routes.PASSKEY_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            PasskeyDetailScreen(
                passkeyId = entry.arguments?.getString("id").orEmpty(),
                onBack = navController::popBackStack,
            )
        }

        // --- Secure notes -------------------------------------------------------
        composable(
            Routes.NOTE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            NoteDetailScreen(
                noteId = entry.arguments?.getString("id").orEmpty(),
                onBack = navController::popBackStack,
                onEdit = { id -> navController.navigate(Routes.noteEdit(id)) },
            )
        }
        composable(Routes.NOTE_ADD) {
            AddEditNoteScreen(noteId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.NOTE_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditNoteScreen(
                noteId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Payment cards -------------------------------------------------------
        composable(
            Routes.CARD_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            CardDetailScreen(
                cardId = entry.arguments?.getString("id").orEmpty(),
                onBack = navController::popBackStack,
                onEdit = { id -> navController.navigate(Routes.cardEdit(id)) },
            )
        }
        composable(Routes.CARD_ADD) {
            AddEditCardScreen(cardId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.CARD_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditCardScreen(
                cardId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Identity -------------------------------------------------------
        composable(
            Routes.IDENTITY_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            IdentityDetailScreen(
                identityId = entry.arguments?.getString("id").orEmpty(),
                onBack = navController::popBackStack,
                onEdit = { id -> navController.navigate(Routes.identityEdit(id)) },
            )
        }
        composable(Routes.IDENTITY_ADD) {
            AddEditIdentityScreen(identityId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.IDENTITY_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditIdentityScreen(
                identityId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Wi-Fi (combined view/edit) -------------------------------------------------------
        composable(Routes.WIFI_ADD) {
            AddEditWifiScreen(wifiId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.WIFI_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditWifiScreen(
                wifiId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- OTP -------------------------------------------------------
        composable(Routes.OTP_LIST) { OtpListScreen(onBack = navController::popBackStack) }

        // --- Documents -------------------------------------------------------
        composable(Routes.DOCUMENTS) {
            DocumentsScreen(
                onBack = navController::popBackStack,
                onOpenDocument = { id -> navController.navigate(Routes.documentDetail(id)) },
            )
        }
        composable(
            Routes.DOCUMENT_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            DocumentDetailScreen(
                documentId = entry.arguments?.getString("id").orEmpty(),
                onBack = navController::popBackStack,
            )
        }

        // --- API keys (combined view/edit) -------------------------------------------------------
        composable(Routes.API_KEY_ADD) {
            AddEditApiKeyScreen(apiKeyId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.API_KEY_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditApiKeyScreen(
                apiKeyId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Recovery codes (combined view/edit) -------------------------------------------------------
        composable(Routes.RECOVERY_CODES_ADD) {
            AddEditRecoveryCodesScreen(entryId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.RECOVERY_CODES_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditRecoveryCodesScreen(
                entryId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Custom items (combined view/edit) -------------------------------------------------------
        composable(Routes.CUSTOM_ITEM_ADD) {
            AddEditCustomItemScreen(itemId = null, onDone = navController::popBackStack, onBack = navController::popBackStack)
        }
        composable(
            Routes.CUSTOM_ITEM_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            AddEditCustomItemScreen(
                itemId = entry.arguments?.getString("id"),
                onDone = navController::popBackStack,
                onBack = navController::popBackStack,
            )
        }

        // --- Security -------------------------------------------------------
        composable(Routes.BREACH_MONITORING) { BreachMonitoringScreen(onBack = navController::popBackStack) }
        composable(
            Routes.SECURITY_ISSUE_DETAIL,
            arguments = listOf(navArgument("type") { type = NavType.StringType }),
        ) { entry ->
            SecurityIssueDetailScreen(
                issueType = entry.arguments?.getString("type").orEmpty(),
                onBack = navController::popBackStack,
                onOpenLogin = { id -> navController.navigate(Routes.passwordDetail(id)) },
            )
        }
    }
}

/** Two categories (Documents, OTP) have a dedicated top-level screen instead of the generic list. */
private fun NavHostController.navigateToCategory(category: String) {
    when (runCatching { VaultCategory.valueOf(category) }.getOrNull()) {
        VaultCategory.DOCUMENT -> navigate(Routes.DOCUMENTS)
        VaultCategory.OTP -> navigate(Routes.OTP_LIST)
        VaultCategory.PASSKEY -> navigate(Routes.PASSKEYS)
        else -> navigate(Routes.vaultCategory(category))
    }
}

private fun NavHostController.navigateToAdd(category: String) {
    when (runCatching { VaultCategory.valueOf(category) }.getOrNull()) {
        VaultCategory.LOGIN -> navigate(Routes.PASSWORD_ADD)
        VaultCategory.PASSKEY -> navigate(Routes.PASSKEYS)
        VaultCategory.SECURE_NOTE -> navigate(Routes.NOTE_ADD)
        VaultCategory.PAYMENT_CARD -> navigate(Routes.CARD_ADD)
        VaultCategory.IDENTITY -> navigate(Routes.IDENTITY_ADD)
        VaultCategory.WIFI -> navigate(Routes.WIFI_ADD)
        VaultCategory.OTP -> navigate(Routes.OTP_LIST)
        VaultCategory.DOCUMENT -> navigate(Routes.DOCUMENTS)
        VaultCategory.API_KEY -> navigate(Routes.API_KEY_ADD)
        VaultCategory.RECOVERY_CODE -> navigate(Routes.RECOVERY_CODES_ADD)
        VaultCategory.CUSTOM, null -> navigate(Routes.CUSTOM_ITEM_ADD)
    }
}

/**
 * [category] is always a [VaultCategory.name] (e.g. "LOGIN", "PAYMENT_CARD") — the vault list,
 * search, home, and this nav host all agree on that as the wire format so routing never has to
 * guess at a free-form display string.
 */
private fun NavHostController.navigateToItem(category: String, id: String) {
    when (runCatching { VaultCategory.valueOf(category) }.getOrNull()) {
        VaultCategory.LOGIN -> navigate(Routes.passwordDetail(id))
        VaultCategory.PASSKEY -> navigate(Routes.passkeyDetail(id))
        VaultCategory.SECURE_NOTE -> navigate(Routes.noteDetail(id))
        VaultCategory.PAYMENT_CARD -> navigate(Routes.cardDetail(id))
        VaultCategory.IDENTITY -> navigate(Routes.identityDetail(id))
        VaultCategory.WIFI -> navigate(Routes.wifiDetail(id))
        VaultCategory.OTP -> navigate(Routes.OTP_LIST)
        VaultCategory.DOCUMENT -> navigate(Routes.documentDetail(id))
        VaultCategory.API_KEY -> navigate(Routes.apiKeyDetail(id))
        VaultCategory.RECOVERY_CODE -> navigate(Routes.recoveryCodesDetail(id))
        VaultCategory.CUSTOM, null -> navigate(Routes.customItemDetail(id))
    }
}
