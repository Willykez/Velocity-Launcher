package com.example.model

enum class GestureAction(val label: String, val iconName: String) {
    NONE("None", "Close"),
    OPEN_DRAWER("Open App Drawer", "Menu"),
    OPEN_NOTIFICATIONS("Expand Notifications", "Notifications"),
    OPEN_SETTINGS("Launcher Settings", "Settings"),
    OPEN_SEARCH("Instant App Search", "Search"),
    LOCK_SCREEN("Lock Screen", "Lock"),
    TOGGLE_WALLPAPER("Cycle Wallpaper Mode", "Wallpaper")
}
