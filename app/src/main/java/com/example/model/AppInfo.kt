package com.example.model

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val category: AppCategory = AppCategory.TOOLS,
    val installTime: Long = 0L,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false
) {
    val displayLabel: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: label
}
