package com.example.model

data class LauncherSettings(
    val gridRows: Int = 5,
    val gridCols: Int = 4,
    val dockCount: Int = 5,
    val iconSizeDp: Int = 54,
    val showLabels: Boolean = true,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val iconPackPackage: String? = null, // null/blank = use each app's own launcher icon
    val themeMode: String = "AMOLED", // AMOLED, DARK, LIGHT, NEON, PASTEL
    val wallpaperStyle: String = "AURA_GRADIENT", // SYSTEM, AURA_GRADIENT, COSMIC_DARK, SUNSET, MINIMAL_BLACK
    val drawerLayout: String = "CATEGORIZED", // CATEGORIZED, DETAILED_LIST, COMPACT
    val doubleTapGesture: GestureAction = GestureAction.OPEN_SETTINGS,
    val swipeDownGesture: GestureAction = GestureAction.OPEN_SEARCH,
    val swipeUpGesture: GestureAction = GestureAction.OPEN_DRAWER,
    val pinchGesture: GestureAction = GestureAction.OPEN_SETTINGS,
    val showAtAGlanceWidget: Boolean = true,
    val badgeMode: String = "DOT", // NONE, DOT, NUMERIC
    val monochromeIcons: Boolean = false,
    val iconLabelSize: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    val wallpaperDim: Float = 0.0f,
    val focusModeEnabled: Boolean = false,
    val appLockPin: String = "",
    val searchEngine: String = "GOOGLE" // GOOGLE, DUCKDUCKGO, YOUTUBE, WIKIPEDIA
)
