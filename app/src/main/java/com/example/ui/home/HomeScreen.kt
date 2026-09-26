package com.example.ui.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import com.example.engine.HapticUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FolderEntity
import com.example.data.LauncherItemEntity
import com.example.engine.AppLoader
import com.example.model.GestureAction
import com.example.ui.backup.BackupRestoreDialog
import com.example.ui.drawer.AppDrawerSheet
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.AuraGradients
import com.example.viewmodel.LauncherViewModel

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeItems by viewModel.homeItems.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val quickNote by viewModel.quickNote.collectAsState()
    val appUsage by viewModel.appUsage.collectAsState()

    val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()
    val drawerSearchQuery by viewModel.drawerSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isBackupDialogOpen by viewModel.isBackupDialogOpen.collectAsState()
    val activeFolder by viewModel.activeFolder.collectAsState()
    val contextMenuItem by viewModel.contextMenuItem.collectAsState()
    val contextMenuApp by viewModel.contextMenuApp.collectAsState()
    val isEditingHome by viewModel.isEditingHome.collectAsState()
    val isAddWidgetOpen by viewModel.isAddWidgetOpen.collectAsState()
    val renameTarget by viewModel.renameTarget.collectAsState()

    var showQuickOptionsModal by remember { mutableStateOf(false) }

    val pageCount = 2
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })

    val wallpaperModifier = when (settings.wallpaperStyle) {
        "AURA_GRADIENT" -> Modifier.background(AuraGradients.aura)
        "COSMIC_DARK" -> Modifier.background(AuraGradients.cosmic)
        "SUNSET" -> Modifier.background(AuraGradients.sunset)
        "MINIMAL_BLACK" -> Modifier.background(AuraGradients.minimalDark)
        else -> Modifier.background(Color.Transparent)
    }

    BackHandler(enabled = isEditingHome) {
        viewModel.setEditingHome(false)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(wallpaperModifier)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                var totalDragY = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        totalDragY += dragAmount
                    },
                    onDragEnd = {
                        if (totalDragY < -60f) {
                            HapticUtils.tick(context)
                            viewModel.executeGesture(settings.swipeUpGesture, context)
                        } else if (totalDragY > 60f) {
                            HapticUtils.tick(context)
                            viewModel.executeGesture(settings.swipeDownGesture, context)
                        }
                        totalDragY = 0f
                    }
                )
            }
            .pointerInput(Unit) {
                // Pinch gesture: settings.pinchGesture was configurable but never
                // actually detected anywhere - this is what fires it. Fires once per
                // pinch past a threshold, not continuously, so it behaves like the
                // other one-shot gestures rather than repeating mid-gesture.
                var cumulativeZoom = 1f
                var fired = false
                detectTransformGestures { _, _, zoom, _ ->
                    cumulativeZoom *= zoom
                    if (!fired && kotlin.math.abs(cumulativeZoom - 1f) > 0.22f) {
                        fired = true
                        HapticUtils.tick(context)
                        viewModel.executeGesture(settings.pinchGesture, context)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        HapticUtils.tick(context)
                        viewModel.executeGesture(settings.doubleTapGesture, context)
                    },
                    onLongPress = {
                        HapticUtils.tick(context)
                        showQuickOptionsModal = true
                    }
                )
            }
            .testTag("home_screen_root")
    ) {
        if (settings.focusModeEnabled) {
            // Minimalist / Focus Mode layout
            FocusModeView(
                apps = apps,
                onAppClick = { app ->
                    viewModel.launchApp(app.packageName, app.activityName)
                },
                onExitFocusMode = {
                    viewModel.updateSettings(settings.copy(focusModeEnabled = false))
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Edit mode top bar
                if (isEditingHome) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Aura Edit Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { viewModel.setEditingHome(false) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(4.dp))
                                Text("Done")
                            }
                        }
                    }
                }

                // Workspace Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    HomeGrid(
                        pageIndex = page,
                        items = homeItems,
                        settings = settings,
                        isEditing = isEditingHome,
                        quickNote = quickNote,
                        onSaveQuickNote = { content, color ->
                            viewModel.saveQuickNote(content, color)
                        },
                        onItemClick = { item ->
                            if (item.type == "FOLDER") {
                                val f = folders.firstOrNull { it.id == item.folderId }
                                    ?: FolderEntity(id = item.folderId ?: 0L, name = item.label, pageIndex = page, cellX = item.cellX, cellY = item.cellY)
                                viewModel.openFolder(f)
                            } else if (!item.packageName.isNullOrBlank()) {
                                viewModel.launchApp(item.packageName, item.activityName)
                            }
                        },
                        onItemLongClick = { item ->
                            viewModel.showItemContextMenu(item)
                        },
                        onRemoveItem = { id ->
                            viewModel.removeHomeItem(id)
                        },
                        onSearchClick = {
                            viewModel.openDrawer()
                        },
                        onEmptySpaceLongClick = {
                            showQuickOptionsModal = true
                        },
                        onOpenAddWidget = {
                            viewModel.openAddWidget()
                        }
                    )
                }

                // Page Indicator Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                        )
                    }
                }

                // Persistent Bottom Dock
                val dockItems = homeItems.filter { it.pageIndex == -1 }
                DockView(
                    dockItems = dockItems,
                    settings = settings,
                    onItemClick = { item ->
                        if (item.type == "FOLDER") {
                            val f = folders.firstOrNull { it.id == item.folderId }
                                ?: FolderEntity(id = item.folderId ?: 0L, name = item.label, pageIndex = -1, cellX = item.cellX, cellY = item.cellY)
                            viewModel.openFolder(f)
                        } else if (!item.packageName.isNullOrBlank()) {
                            viewModel.launchApp(item.packageName, item.activityName)
                        }
                    },
                    onItemLongClick = { item ->
                        viewModel.showItemContextMenu(item)
                    },
                    onOpenDrawerClick = {
                        viewModel.openDrawer()
                    }
                )
            }
        }

        // Quick Options Popup Modal
        if (showQuickOptionsModal) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Workspace Customization",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.setEditingHome(true)
                                showQuickOptionsModal = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Edit Grid")
                        }

                        Button(
                            onClick = {
                                viewModel.openAddWidget()
                                showQuickOptionsModal = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Widgets")
                        }

                        Button(
                            onClick = {
                                viewModel.openSettings()
                                showQuickOptionsModal = false
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Settings")
                        }
                    }
                }
            }
        }

        // App Drawer Full Sheet
        AppDrawerSheet(
            isOpen = isDrawerOpen,
            apps = apps,
            usageList = appUsage,
            searchQuery = drawerSearchQuery,
            selectedCategory = selectedCategory,
            settings = settings,
            onSearchChange = { viewModel.setDrawerSearchQuery(it) },
            onCategoryChange = { viewModel.setDrawerCategory(it) },
            onAppClick = { app ->
                viewModel.launchApp(app.packageName, app.activityName)
                viewModel.closeDrawer()
            },
            onAppLongClick = { app ->
                viewModel.showAppContextMenu(app)
            },
            onClose = { viewModel.closeDrawer() }
        )

        // Settings Screen
        if (isSettingsOpen) {
            SettingsScreen(
                settings = settings,
                appUsage = appUsage,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onOpenBackup = { viewModel.openBackupDialog() },
                onResetDefaults = { viewModel.resetLayoutToDefault() },
                onClose = { viewModel.closeSettings() }
            )
        }

        // Folder Overlay
        activeFolder?.let { folder ->
            val itemsInFolder = homeItems.filter { it.folderId == folder.id }
            FolderOverlay(
                folder = folder,
                itemsInFolder = itemsInFolder,
                settings = settings,
                onDismiss = { viewModel.closeFolder() },
                onAppClick = { item ->
                    if (!item.packageName.isNullOrBlank()) {
                        viewModel.launchApp(item.packageName, item.activityName)
                    }
                },
                onAppLongClick = { item ->
                    viewModel.showItemContextMenu(item)
                },
                onRenameFolder = { newName ->
                    viewModel.renameFolder(folder.id, newName)
                },
                onDeleteFolder = {
                    viewModel.deleteFolder(folder.id)
                }
            )
        }

        // Context Menu for Home Item
        contextMenuItem?.let { item ->
            val matchingApp = apps.firstOrNull { it.packageName == item.packageName }
            ItemContextMenu(
                title = item.label,
                packageName = item.packageName,
                isHomeItem = true,
                isFavorite = matchingApp?.isFavorite ?: false,
                isHidden = matchingApp?.isHidden ?: false,
                onDismiss = { viewModel.hideItemContextMenu() },
                onToggleFavorite = if (item.packageName != null) {
                    { viewModel.toggleAppFavorite(item.packageName) }
                } else null,
                onRename = {
                    val app = matchingApp ?: com.example.model.AppInfo(
                        packageName = item.packageName ?: "",
                        activityName = item.activityName ?: "",
                        label = item.label
                    )
                    viewModel.startRename(app)
                },
                onAppInfo = {
                    item.packageName?.let { AppLoader.openAppInfo(context, it) }
                    viewModel.hideItemContextMenu()
                },
                onRemoveFromHome = {
                    viewModel.removeHomeItem(item.id)
                },
                onUninstall = {
                    item.packageName?.let { AppLoader.requestUninstall(context, it) }
                    viewModel.hideItemContextMenu()
                }
            )
        }

        // Context Menu for Drawer App
        contextMenuApp?.let { app ->
            ItemContextMenu(
                title = app.displayLabel,
                packageName = app.packageName,
                isHomeItem = false,
                isFavorite = app.isFavorite,
                isHidden = app.isHidden,
                onDismiss = { viewModel.hideAppContextMenu() },
                onAddToHome = {
                    viewModel.addAppToHome(app, pagerState.currentPage)
                },
                onToggleFavorite = {
                    viewModel.toggleAppFavorite(app.packageName)
                },
                onToggleHidden = {
                    viewModel.toggleAppHidden(app.packageName)
                },
                onRename = {
                    viewModel.startRename(app)
                },
                onAppInfo = {
                    AppLoader.openAppInfo(context, app.packageName)
                    viewModel.hideAppContextMenu()
                },
                onUninstall = {
                    AppLoader.requestUninstall(context, app.packageName)
                    viewModel.hideAppContextMenu()
                }
            )
        }

        // Backup & Restore Dialog
        if (isBackupDialogOpen) {
            BackupRestoreDialog(
                onDismiss = { viewModel.closeBackupDialog() },
                onExportJson = { viewModel.exportJson() },
                onImportJson = { json, cb -> viewModel.importJson(json, cb) }
            )
        }

        // Widget Picker Dialog
        if (isAddWidgetOpen) {
            WidgetPickerDialog(
                onDismiss = { viewModel.closeAddWidget() },
                onSelectWidget = { widgetType ->
                    viewModel.addWidgetToHome(widgetType, pagerState.currentPage)
                }
            )
        }

        // Rename App Dialog
        renameTarget?.let { app ->
            RenameAppDialog(
                initialName = app.displayLabel,
                onDismiss = { viewModel.dismissRename() },
                onConfirm = { newName ->
                    viewModel.saveRenamedApp(app.packageName, newName)
                }
            )
        }
    }
}
