package com.velora.vault.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.velora.vault.core.design.components.RadialAddMenu
import com.velora.vault.core.design.components.VeloraBottomBar
import com.velora.vault.core.design.components.VeloraBottomNavTabs
import com.velora.vault.core.design.components.defaultRadialActions

/** Top-level tab routes that show the bottom navigation bar. */
private val bottomBarRoutes = VeloraBottomNavTabs.map { it.route }.toSet()

@Composable
fun VeloraApp(launchAction: String? = null) {
    val rootViewModel: AppRootViewModel = hiltViewModel()
    val rootState by rootViewModel.state.collectAsState()
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes
    var showAddMenu by remember { mutableStateOf(false) }

    LaunchedEffect(rootState) {
        when (rootState) {
            AppRootState.Loading -> Unit
            AppRootState.NeedsOnboarding -> navController.navigateAsRoot(Routes.ONBOARDING)
            AppRootState.NeedsAccountSetup -> navController.navigateAsRoot(Routes.SIGN_UP)
            AppRootState.NeedsUnlock -> navController.navigateAsRoot(Routes.UNLOCK)
            AppRootState.Ready -> {
                navController.navigateAsRoot(Routes.VAULT_HOME)
                when (launchAction) {
                    "com.velora.vault.action.ADD_PASSWORD" -> navController.navigate(Routes.PASSWORD_ADD)
                    "com.velora.vault.action.GENERATE_PASSWORD" -> navController.navigate(Routes.GENERATOR)
                    "com.velora.vault.action.SEARCH_VAULT" -> navController.navigate(Routes.SEARCH)
                    else -> Unit
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    VeloraBottomBar(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(Routes.VAULT_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onAddClick = { showAddMenu = true },
                    )
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                VeloraNavHost(navController = navController, launchAction = launchAction)
            }
        }

        RadialAddMenu(
            visible = showAddMenu,
            onDismiss = { showAddMenu = false },
            actions = defaultRadialActions(
                onPassword = { showAddMenu = false; navController.navigate(Routes.PASSWORD_ADD) },
                onPasskey = { showAddMenu = false; navController.navigate(Routes.PASSKEYS) },
                onNote = { showAddMenu = false; navController.navigate(Routes.NOTE_ADD) },
                onCard = { showAddMenu = false; navController.navigate(Routes.CARD_ADD) },
            ),
        )
    }
}

private fun NavHostController.navigateAsRoot(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
