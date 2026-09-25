package com.example.model

enum class AppCategory(val title: String, val iconName: String) {
    ALL("All", "Apps"),
    FAVORITES("Favorites", "Star"),
    SOCIAL("Social & Chat", "Chat"),
    MEDIA("Media & Video", "Play"),
    PRODUCTIVITY("Work & Productivity", "Work"),
    TOOLS("Tools & System", "Build"),
    GAMES("Games", "SportsEsports"),
    HIDDEN("Hidden Apps", "VisibilityOff")
}
