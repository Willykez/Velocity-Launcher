package com.example

import android.app.Application
import com.example.data.LauncherDatabase
import com.example.data.LauncherRepository

class AuraLauncherApplication : Application() {
    val database by lazy { LauncherDatabase.getDatabase(this) }
    val repository by lazy { LauncherRepository(database.launcherDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
