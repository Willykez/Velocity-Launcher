package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LauncherItemEntity::class,
        FolderEntity::class,
        AppOverrideEntity::class,
        LauncherSettingsEntity::class,
        AppUsageEntity::class,
        QuickNoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getDatabase(context: Context): LauncherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "aura_launcher.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
