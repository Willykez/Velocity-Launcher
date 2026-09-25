package com.example.data

import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class LauncherRepository(private val dao: LauncherDao) {

    val allItems: Flow<List<LauncherItemEntity>> = dao.getAllItems()
    val allFolders: Flow<List<FolderEntity>> = dao.getAllFolders()
    val allOverrides: Flow<List<AppOverrideEntity>> = dao.getAllOverrides()
    val quickNote: Flow<QuickNoteEntity?> = dao.getQuickNote()
    val appUsage: Flow<List<AppUsageEntity>> = dao.getAllUsage()

    val settingsFlow: Flow<LauncherSettings> = dao.getSettings().map { entity ->
        if (entity != null) {
            LauncherSettings(
                gridRows = entity.gridRows,
                gridCols = entity.gridCols,
                dockCount = entity.dockCount,
                iconSizeDp = entity.iconSizeDp,
                showLabels = entity.showLabels,
                iconShape = runCatching { IconShape.valueOf(entity.iconShape) }.getOrDefault(IconShape.SQUIRCLE),
                themeMode = entity.themeMode,
                wallpaperStyle = entity.wallpaperStyle,
                drawerLayout = entity.drawerLayout,
                doubleTapGesture = runCatching { GestureAction.valueOf(entity.doubleTapGesture) }.getOrDefault(GestureAction.OPEN_SETTINGS),
                swipeDownGesture = runCatching { GestureAction.valueOf(entity.swipeDownGesture) }.getOrDefault(GestureAction.OPEN_SEARCH),
                swipeUpGesture = runCatching { GestureAction.valueOf(entity.swipeUpGesture) }.getOrDefault(GestureAction.OPEN_DRAWER),
                pinchGesture = runCatching { GestureAction.valueOf(entity.pinchGesture) }.getOrDefault(GestureAction.OPEN_SETTINGS),
                showAtAGlanceWidget = entity.showAtAGlanceWidget,
                badgeMode = entity.badgeMode,
                monochromeIcons = entity.monochromeIcons,
                iconLabelSize = entity.iconLabelSize,
                wallpaperDim = entity.wallpaperDim,
                focusModeEnabled = entity.focusModeEnabled,
                appLockPin = entity.appLockPin,
                searchEngine = entity.searchEngine
            )
        } else {
            LauncherSettings()
        }
    }

    suspend fun saveSettings(settings: LauncherSettings) {
        dao.insertSettings(
            LauncherSettingsEntity(
                id = 1,
                gridRows = settings.gridRows,
                gridCols = settings.gridCols,
                dockCount = settings.dockCount,
                iconSizeDp = settings.iconSizeDp,
                showLabels = settings.showLabels,
                iconShape = settings.iconShape.name,
                themeMode = settings.themeMode,
                wallpaperStyle = settings.wallpaperStyle,
                drawerLayout = settings.drawerLayout,
                doubleTapGesture = settings.doubleTapGesture.name,
                swipeDownGesture = settings.swipeDownGesture.name,
                swipeUpGesture = settings.swipeUpGesture.name,
                pinchGesture = settings.pinchGesture.name,
                showAtAGlanceWidget = settings.showAtAGlanceWidget,
                badgeMode = settings.badgeMode,
                monochromeIcons = settings.monochromeIcons,
                iconLabelSize = settings.iconLabelSize,
                wallpaperDim = settings.wallpaperDim,
                focusModeEnabled = settings.focusModeEnabled,
                appLockPin = settings.appLockPin,
                searchEngine = settings.searchEngine
            )
        )
    }

    suspend fun addItem(item: LauncherItemEntity): Long = dao.insertItem(item)

    suspend fun updateItem(item: LauncherItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(id: Long) = dao.deleteItem(id)

    suspend fun addFolder(name: String, pageIndex: Int, cellX: Int, cellY: Int): Long {
        return dao.insertFolder(
            FolderEntity(name = name, pageIndex = pageIndex, cellX = cellX, cellY = cellY)
        )
    }

    suspend fun updateFolder(folder: FolderEntity) = dao.updateFolder(folder)

    suspend fun deleteFolder(id: Long) {
        dao.deleteFolder(id)
        dao.deleteItemsInFolder(id)
    }

    suspend fun saveAppOverride(
        packageName: String,
        customLabel: String?,
        isFavorite: Boolean,
        isHidden: Boolean,
        customCategory: String? = null
    ) {
        dao.insertOverride(
            AppOverrideEntity(
                packageName = packageName,
                customLabel = customLabel,
                isFavorite = isFavorite,
                isHidden = isHidden,
                customCategory = customCategory
            )
        )
    }

    suspend fun recordAppLaunch(packageName: String) {
        val existing = dao.getUsageForPackage(packageName)
        if (existing != null) {
            dao.insertUsage(existing.copy(launchCount = existing.launchCount + 1, lastLaunched = System.currentTimeMillis()))
        } else {
            dao.insertUsage(AppUsageEntity(packageName = packageName, launchCount = 1, lastLaunched = System.currentTimeMillis()))
        }
    }

    suspend fun saveQuickNote(content: String, colorHex: String) {
        dao.insertQuickNote(QuickNoteEntity(id = 1, content = content, colorHex = colorHex, updatedAt = System.currentTimeMillis()))
    }

    suspend fun clearAll() {
        dao.clearAllItems()
    }

    suspend fun exportLayoutJson(currentItems: List<LauncherItemEntity>, currentFolders: List<FolderEntity>, settings: LauncherSettings): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("timestamp", System.currentTimeMillis())

        val settingsObj = JSONObject().apply {
            put("gridRows", settings.gridRows)
            put("gridCols", settings.gridCols)
            put("dockCount", settings.dockCount)
            put("iconSizeDp", settings.iconSizeDp)
            put("showLabels", settings.showLabels)
            put("iconShape", settings.iconShape.name)
            put("themeMode", settings.themeMode)
            put("wallpaperStyle", settings.wallpaperStyle)
            put("drawerLayout", settings.drawerLayout)
            put("showAtAGlanceWidget", settings.showAtAGlanceWidget)
            put("badgeMode", settings.badgeMode)
            put("monochromeIcons", settings.monochromeIcons)
            put("iconLabelSize", settings.iconLabelSize)
            put("wallpaperDim", settings.wallpaperDim.toDouble())
            put("focusModeEnabled", settings.focusModeEnabled)
            put("searchEngine", settings.searchEngine)
        }
        root.put("settings", settingsObj)

        val itemsArray = JSONArray()
        currentItems.forEach { item ->
            val obj = JSONObject().apply {
                put("type", item.type)
                put("packageName", item.packageName ?: "")
                put("activityName", item.activityName ?: "")
                put("label", item.label)
                put("pageIndex", item.pageIndex)
                put("cellX", item.cellX)
                put("cellY", item.cellY)
                put("folderId", item.folderId ?: -1L)
                put("folderName", item.folderName ?: "")
                put("widgetType", item.widgetType ?: "")
            }
            itemsArray.put(obj)
        }
        root.put("items", itemsArray)

        val foldersArray = JSONArray()
        currentFolders.forEach { folder ->
            val obj = JSONObject().apply {
                put("id", folder.id)
                put("name", folder.name)
                put("pageIndex", folder.pageIndex)
                put("cellX", folder.cellX)
                put("cellY", folder.cellY)
            }
            foldersArray.put(obj)
        }
        root.put("folders", foldersArray)

        return root.toString(2)
    }

    suspend fun importLayoutJson(jsonString: String) {
        val root = JSONObject(jsonString)
        if (root.has("settings")) {
            val s = root.getJSONObject("settings")
            val newSettings = LauncherSettings(
                gridRows = s.optInt("gridRows", 5),
                gridCols = s.optInt("gridCols", 4),
                dockCount = s.optInt("dockCount", 5),
                iconSizeDp = s.optInt("iconSizeDp", 54),
                showLabels = s.optBoolean("showLabels", true),
                iconShape = runCatching { IconShape.valueOf(s.optString("iconShape", "SQUIRCLE")) }.getOrDefault(IconShape.SQUIRCLE),
                themeMode = s.optString("themeMode", "AMOLED"),
                wallpaperStyle = s.optString("wallpaperStyle", "AURA_GRADIENT"),
                drawerLayout = s.optString("drawerLayout", "CATEGORIZED"),
                showAtAGlanceWidget = s.optBoolean("showAtAGlanceWidget", true),
                badgeMode = s.optString("badgeMode", "DOT"),
                monochromeIcons = s.optBoolean("monochromeIcons", false),
                iconLabelSize = s.optString("iconLabelSize", "MEDIUM"),
                wallpaperDim = s.optDouble("wallpaperDim", 0.0).toFloat(),
                focusModeEnabled = s.optBoolean("focusModeEnabled", false),
                searchEngine = s.optString("searchEngine", "GOOGLE")
            )
            saveSettings(newSettings)
        }

        dao.clearAllItems()

        if (root.has("items")) {
            val arr = root.getJSONArray("items")
            val list = mutableListOf<LauncherItemEntity>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    LauncherItemEntity(
                        type = obj.optString("type", "APP"),
                        packageName = obj.optString("packageName").takeIf { it.isNotBlank() },
                        activityName = obj.optString("activityName").takeIf { it.isNotBlank() },
                        label = obj.optString("label", ""),
                        pageIndex = obj.optInt("pageIndex", 0),
                        cellX = obj.optInt("cellX", 0),
                        cellY = obj.optInt("cellY", 0),
                        folderId = obj.optLong("folderId", -1L).takeIf { it > 0 },
                        folderName = obj.optString("folderName").takeIf { it.isNotBlank() },
                        widgetType = obj.optString("widgetType").takeIf { it.isNotBlank() }
                    )
                )
            }
            dao.insertItems(list)
        }
    }
}
