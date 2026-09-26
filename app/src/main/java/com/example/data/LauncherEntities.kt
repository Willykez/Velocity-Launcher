package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_items")
data class LauncherItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "APP", "FOLDER", "WIDGET"
    val packageName: String?,
    val activityName: String?,
    val label: String,
    val pageIndex: Int, // -1 for dock, 0+ for home screen pages
    val cellX: Int,
    val cellY: Int,
    val folderId: Long? = null,
    val folderName: String? = null,
    val widgetType: String? = null // "AT_A_GLANCE", "QUICK_SEARCH", "SYSTEM_TELEMETRY", "QUICK_NOTE", "MUSIC_PLAYER"
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val pageIndex: Int,
    val cellX: Int,
    val cellY: Int
)

@Entity(tableName = "app_overrides")
data class AppOverrideEntity(
    @PrimaryKey
    val packageName: String,
    val customLabel: String? = null,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val customCategory: String? = null
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey
    val packageName: String,
    val launchCount: Int = 1,
    val lastLaunched: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_notes")
data class QuickNoteEntity(
    @PrimaryKey
    val id: Long = 1,
    val content: String = "Welcome to Aura Launcher! Tap here to keep quick thoughts, todo lists, or reminders on your home screen.",
    val colorHex: String = "#6C5CE7",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "launcher_settings")
data class LauncherSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val gridRows: Int = 5,
    val gridCols: Int = 4,
    val dockCount: Int = 5,
    val iconSizeDp: Int = 54,
    val showLabels: Boolean = true,
    val iconShape: String = "SQUIRCLE",
    val iconPackPackage: String? = null,
    val themeMode: String = "AMOLED",
    val wallpaperStyle: String = "AURA_GRADIENT",
    val drawerLayout: String = "CATEGORIZED",
    val doubleTapGesture: String = "OPEN_SETTINGS",
    val swipeDownGesture: String = "OPEN_SEARCH",
    val swipeUpGesture: String = "OPEN_DRAWER",
    val pinchGesture: String = "OPEN_SETTINGS",
    val showAtAGlanceWidget: Boolean = true,
    val badgeMode: String = "DOT", // "NONE", "DOT", "NUMERIC"
    val monochromeIcons: Boolean = false,
    val iconLabelSize: String = "MEDIUM", // "SMALL", "MEDIUM", "LARGE"
    val wallpaperDim: Float = 0.0f,
    val focusModeEnabled: Boolean = false,
    val appLockPin: String = "",
    val searchEngine: String = "GOOGLE"
)
