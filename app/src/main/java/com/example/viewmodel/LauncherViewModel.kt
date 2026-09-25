package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppOverrideEntity
import com.example.data.AppUsageEntity
import com.example.data.FolderEntity
import com.example.data.LauncherItemEntity
import com.example.data.LauncherRepository
import com.example.data.QuickNoteEntity
import com.example.engine.AppLoader
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.GestureAction
import com.example.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val repository: LauncherRepository,
    private val appContext: Context
) : ViewModel() {

    private val _rawInstalledApps = MutableStateFlow<List<AppInfo>>(emptyList())

    val homeItems: StateFlow<List<LauncherItemEntity>> = repository.allItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val overrides: StateFlow<List<AppOverrideEntity>> = repository.allOverrides.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings: StateFlow<LauncherSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherSettings()
    )

    val quickNote: StateFlow<QuickNoteEntity?> = repository.quickNote.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val appUsage: StateFlow<List<AppUsageEntity>> = repository.appUsage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Merged apps with custom labels, hidden status, and favorites
    val apps: StateFlow<List<AppInfo>> = combine(_rawInstalledApps, overrides) { raw, ovList ->
        val ovMap = ovList.associateBy { it.packageName }
        raw.map { app ->
            val ov = ovMap[app.packageName]
            if (ov != null) {
                app.copy(
                    customLabel = ov.customLabel,
                    isFavorite = ov.isFavorite,
                    isHidden = ov.isHidden
                )
            } else {
                app
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // UI Transient States
    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _drawerSearchQuery = MutableStateFlow("")
    val drawerSearchQuery: StateFlow<String> = _drawerSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isBackupDialogOpen = MutableStateFlow(false)
    val isBackupDialogOpen: StateFlow<Boolean> = _isBackupDialogOpen.asStateFlow()

    private val _activeFolder = MutableStateFlow<FolderEntity?>(null)
    val activeFolder: StateFlow<FolderEntity?> = _activeFolder.asStateFlow()

    private val _contextMenuItem = MutableStateFlow<LauncherItemEntity?>(null)
    val contextMenuItem: StateFlow<LauncherItemEntity?> = _contextMenuItem.asStateFlow()

    private val _contextMenuApp = MutableStateFlow<AppInfo?>(null)
    val contextMenuApp: StateFlow<AppInfo?> = _contextMenuApp.asStateFlow()

    private val _isEditingHome = MutableStateFlow(false)
    val isEditingHome: StateFlow<Boolean> = _isEditingHome.asStateFlow()

    private val _isAddWidgetOpen = MutableStateFlow(false)
    val isAddWidgetOpen: StateFlow<Boolean> = _isAddWidgetOpen.asStateFlow()

    private val _renameTarget = MutableStateFlow<AppInfo?>(null)
    val renameTarget: StateFlow<AppInfo?> = _renameTarget.asStateFlow()

    init {
        refreshInstalledApps()
        checkDefaultLayout()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = AppLoader.loadInstalledApps(appContext)
            _rawInstalledApps.value = apps
            checkDefaultLayout()
        }
    }

    private fun checkDefaultLayout() {
        viewModelScope.launch {
            homeItems.collect { items ->
                if (items.isEmpty() && _rawInstalledApps.value.isNotEmpty()) {
                    setupDefaultLayout(_rawInstalledApps.value)
                }
            }
        }
    }

    private suspend fun setupDefaultLayout(availableApps: List<AppInfo>) {
        val initialItems = mutableListOf<LauncherItemEntity>()

        // 1. Top At-A-Glance Widget on Page 0
        initialItems.add(
            LauncherItemEntity(
                type = "WIDGET",
                packageName = null,
                activityName = null,
                label = "At a Glance",
                pageIndex = 0,
                cellX = 0,
                cellY = 0,
                widgetType = "AT_A_GLANCE"
            )
        )

        // 2. Populate Dock (pageIndex = -1) with up to 4 core apps
        val dockApps = availableApps.take(4)
        dockApps.forEachIndexed { index, app ->
            initialItems.add(
                LauncherItemEntity(
                    type = "APP",
                    packageName = app.packageName,
                    activityName = app.activityName,
                    label = app.label,
                    pageIndex = -1,
                    cellX = index,
                    cellY = 0
                )
            )
        }

        // 3. Populate Home Screen Grid on Page 0 with remaining apps
        val homeGridApps = availableApps.drop(4).take(8)
        homeGridApps.forEachIndexed { index, app ->
            val row = 2 + (index / 4)
            val col = index % 4
            initialItems.add(
                LauncherItemEntity(
                    type = "APP",
                    packageName = app.packageName,
                    activityName = app.activityName,
                    label = app.label,
                    pageIndex = 0,
                    cellX = col,
                    cellY = row
                )
            )
        }

        for (item in initialItems) {
            repository.addItem(item)
        }
    }

    // Launch app and log usage
    fun launchApp(packageName: String, activityName: String? = null) {
        viewModelScope.launch {
            repository.recordAppLaunch(packageName)
        }
        AppLoader.launchApp(appContext, packageName, activityName)
    }

    // UI actions
    fun openDrawer() { _isDrawerOpen.value = true }
    fun closeDrawer() {
        _isDrawerOpen.value = false
        _drawerSearchQuery.value = ""
    }

    fun setDrawerSearchQuery(query: String) { _drawerSearchQuery.value = query }
    fun setDrawerCategory(cat: AppCategory) { _selectedCategory.value = cat }

    fun openSettings() { _isSettingsOpen.value = true }
    fun closeSettings() { _isSettingsOpen.value = false }

    fun openBackupDialog() { _isBackupDialogOpen.value = true }
    fun closeBackupDialog() { _isBackupDialogOpen.value = false }

    fun openFolder(folder: FolderEntity) { _activeFolder.value = folder }
    fun closeFolder() { _activeFolder.value = null }

    fun showItemContextMenu(item: LauncherItemEntity) { _contextMenuItem.value = item }
    fun hideItemContextMenu() { _contextMenuItem.value = null }

    fun showAppContextMenu(app: AppInfo) { _contextMenuApp.value = app }
    fun hideAppContextMenu() { _contextMenuApp.value = null }

    fun toggleEditHome() { _isEditingHome.value = !_isEditingHome.value }
    fun setEditingHome(editing: Boolean) { _isEditingHome.value = editing }

    fun openAddWidget() { _isAddWidgetOpen.value = true }
    fun closeAddWidget() { _isAddWidgetOpen.value = false }

    fun startRename(app: AppInfo) {
        _renameTarget.value = app
        hideAppContextMenu()
        hideItemContextMenu()
    }
    fun dismissRename() { _renameTarget.value = null }

    fun saveRenamedApp(packageName: String, newName: String) {
        viewModelScope.launch {
            val existing = overrides.value.firstOrNull { it.packageName == packageName }
            repository.saveAppOverride(
                packageName = packageName,
                customLabel = newName,
                isFavorite = existing?.isFavorite ?: false,
                isHidden = existing?.isHidden ?: false
            )
            _renameTarget.value = null
        }
    }

    fun toggleAppHidden(packageName: String) {
        viewModelScope.launch {
            val existing = overrides.value.firstOrNull { it.packageName == packageName }
            val currentHidden = existing?.isHidden ?: false
            repository.saveAppOverride(
                packageName = packageName,
                customLabel = existing?.customLabel,
                isFavorite = existing?.isFavorite ?: false,
                isHidden = !currentHidden
            )
            hideAppContextMenu()
        }
    }

    fun toggleAppFavorite(packageName: String) {
        viewModelScope.launch {
            val existing = overrides.value.firstOrNull { it.packageName == packageName }
            val currentFav = existing?.isFavorite ?: false
            repository.saveAppOverride(
                packageName = packageName,
                customLabel = existing?.customLabel,
                isFavorite = !currentFav,
                isHidden = existing?.isHidden ?: false
            )
            hideAppContextMenu()
        }
    }

    fun addAppToHome(app: AppInfo, page: Int = 0) {
        viewModelScope.launch {
            val occupied = homeItems.value.filter { it.pageIndex == page }
            var targetX = 0
            var targetY = 2
            outer@ for (y in 2..5) {
                for (x in 0..3) {
                    if (occupied.none { it.cellX == x && it.cellY == y }) {
                        targetX = x
                        targetY = y
                        break@outer
                    }
                }
            }

            repository.addItem(
                LauncherItemEntity(
                    type = "APP",
                    packageName = app.packageName,
                    activityName = app.activityName,
                    label = app.displayLabel,
                    pageIndex = page,
                    cellX = targetX,
                    cellY = targetY
                )
            )
            closeDrawer()
            hideAppContextMenu()
        }
    }

    fun addWidgetToHome(widgetType: String, page: Int = 0) {
        viewModelScope.launch {
            repository.addItem(
                LauncherItemEntity(
                    type = "WIDGET",
                    packageName = null,
                    activityName = null,
                    label = when (widgetType) {
                        "SYSTEM_TELEMETRY" -> "System Telemetry"
                        "QUICK_NOTE" -> "Quick Note"
                        "MUSIC_PLAYER" -> "Music Controller"
                        else -> "Widget"
                    },
                    pageIndex = page,
                    cellX = 0,
                    cellY = 1,
                    widgetType = widgetType
                )
            )
            closeAddWidget()
        }
    }

    fun removeHomeItem(id: Long) {
        viewModelScope.launch {
            repository.deleteItem(id)
            hideItemContextMenu()
        }
    }

    fun saveQuickNote(content: String, colorHex: String) {
        viewModelScope.launch {
            repository.saveQuickNote(content, colorHex)
        }
    }

    fun createFolder(name: String, pageIndex: Int, cellX: Int, cellY: Int, initialApps: List<AppInfo>) {
        viewModelScope.launch {
            val folderId = repository.addFolder(name, pageIndex, cellX, cellY)
            initialApps.forEachIndexed { idx, app ->
                repository.addItem(
                    LauncherItemEntity(
                        type = "APP",
                        packageName = app.packageName,
                        activityName = app.activityName,
                        label = app.displayLabel,
                        pageIndex = pageIndex,
                        cellX = idx,
                        cellY = 0,
                        folderId = folderId,
                        folderName = name
                    )
                )
            }
        }
    }

    fun renameFolder(folderId: Long, newName: String) {
        viewModelScope.launch {
            val current = folders.value.firstOrNull { it.id == folderId } ?: return@launch
            repository.updateFolder(current.copy(name = newName))
            _activeFolder.value = current.copy(name = newName)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            closeFolder()
        }
    }

    fun removeAppFromFolder(itemId: Long) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }

    fun updateSettings(newSettings: LauncherSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    fun executeGesture(gestureAction: GestureAction, context: Context) {
        when (gestureAction) {
            GestureAction.OPEN_DRAWER -> openDrawer()
            GestureAction.OPEN_SEARCH -> openDrawer()
            GestureAction.OPEN_SETTINGS -> openSettings()
            GestureAction.OPEN_NOTIFICATIONS -> AppLoader.expandNotificationShade(context)
            GestureAction.TOGGLE_WALLPAPER -> {
                val styles = listOf("SYSTEM", "AURA_GRADIENT", "COSMIC_DARK", "SUNSET", "MINIMAL_BLACK")
                val curIdx = styles.indexOf(settings.value.wallpaperStyle)
                val next = styles[(curIdx + 1) % styles.size]
                updateSettings(settings.value.copy(wallpaperStyle = next))
            }
            GestureAction.LOCK_SCREEN -> {}
            GestureAction.NONE -> {}
        }
    }

    suspend fun exportJson(): String {
        return repository.exportLayoutJson(homeItems.value, folders.value, settings.value)
    }

    fun importJson(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.importLayoutJson(json)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun resetLayoutToDefault() {
        viewModelScope.launch {
            repository.clearAll()
            setupDefaultLayout(_rawInstalledApps.value)
        }
    }
}

class LauncherViewModelFactory(
    private val repository: LauncherRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LauncherViewModel(repository, appContext) as T
    }
}
