package com.velora.vault

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.database.SQLiteDatabase

@HiltAndroidApp
class VeloraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Loads SQLCipher's native libraries once, before any encrypted
        // database is opened (see VeloraDatabaseProvider).
        SQLiteDatabase.loadLibs(this)
    }
}
