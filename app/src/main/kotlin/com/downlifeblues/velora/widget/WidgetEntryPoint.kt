package com.downlifeblues.velora.widget

import com.downlifeblues.velora.core.security.VaultSessionManager
import com.downlifeblues.velora.data.repository.SecurityRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Glance widgets aren't part of the Compose/Activity graph Hilt normally
 * injects into, so they reach their dependencies through this small
 * application-scoped entry point instead of constructor injection.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetDependencies {
    fun vaultSessionManager(): VaultSessionManager
    fun securityRepository(): SecurityRepository
}

fun android.content.Context.widgetDependencies(): WidgetDependencies =
    EntryPointAccessors.fromApplication(applicationContext, WidgetDependencies::class.java)
