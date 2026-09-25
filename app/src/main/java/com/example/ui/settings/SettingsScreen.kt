package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppUsageEntity
import com.example.model.GestureAction
import com.example.model.IconShape
import com.example.model.LauncherSettings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: LauncherSettings,
    appUsage: List<AppUsageEntity> = emptyList(),
    onSettingsChanged: (LauncherSettings) -> Unit,
    onOpenBackup: () -> Unit,
    onResetDefaults: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showResetConfirm by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf(settings.appLockPin) }

    BackHandler {
        onClose()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text("Aura Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Default Launcher Banner
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set as Default Launcher",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Make Aura your primary home experience",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set", fontSize = 12.sp)
                    }
                }
            }

            // Section 1: Focus Mode / Minimalist Toggle
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.focusModeEnabled) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Minimalist Focus Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Distraction-free typographic home layout", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = settings.focusModeEnabled,
                        onCheckedChange = { onSettingsChanged(settings.copy(focusModeEnabled = it)) }
                    )
                }
            }

            // Section 2: Home Screen Grid
            SettingsSectionHeader(title = "Home Screen Grid", icon = Icons.Default.Widgets)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Columns slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Columns (${settings.gridCols})", fontSize = 14.sp)
                        Text("${settings.gridCols} items/row", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = settings.gridCols.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(gridCols = it.roundToInt())) },
                        valueRange = 3f..6f,
                        steps = 2
                    )

                    // Rows slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rows (${settings.gridRows})", fontSize = 14.sp)
                        Text("${settings.gridRows} rows", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = settings.gridRows.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(gridRows = it.roundToInt())) },
                        valueRange = 4f..8f,
                        steps = 3
                    )

                    // Dock Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dock Icons (${settings.dockCount})", fontSize = 14.sp)
                        Text("${settings.dockCount} apps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = settings.dockCount.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(dockCount = it.roundToInt())) },
                        valueRange = 3f..6f,
                        steps = 2
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Labels toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show App Labels", fontSize = 14.sp)
                        Switch(
                            checked = settings.showLabels,
                            onCheckedChange = { onSettingsChanged(settings.copy(showLabels = it)) }
                        )
                    }

                    // At-A-Glance Widget toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("At-A-Glance Widget", fontSize = 14.sp)
                        Switch(
                            checked = settings.showAtAGlanceWidget,
                            onCheckedChange = { onSettingsChanged(settings.copy(showAtAGlanceWidget = it)) }
                        )
                    }
                }
            }

            // Section 3: Adaptive Icons & Shapes
            SettingsSectionHeader(title = "Adaptive Icons & Shapes", icon = Icons.Default.Palette)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select Adaptive Icon Shape:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    // Shape selector chips/preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconShape.values().take(3).forEach { shape ->
                            ShapePreviewBox(
                                shape = shape,
                                isSelected = settings.iconShape == shape,
                                onSelect = { onSettingsChanged(settings.copy(iconShape = shape)) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconShape.values().drop(3).forEach { shape ->
                            ShapePreviewBox(
                                shape = shape,
                                isSelected = settings.iconShape == shape,
                                onSelect = { onSettingsChanged(settings.copy(iconShape = shape)) }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Monochrome Tinted Icons Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Material You Tinted Icons", fontSize = 14.sp)
                            Text("Unify all icons with theme accent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.monochromeIcons,
                            onCheckedChange = { onSettingsChanged(settings.copy(monochromeIcons = it)) }
                        )
                    }

                    // Notification Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notification Badges", fontSize = 14.sp)
                        BadgeDropdown(
                            current = settings.badgeMode,
                            onSelected = { onSettingsChanged(settings.copy(badgeMode = it)) }
                        )
                    }

                    // Icon size slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Icon Size (${settings.iconSizeDp}dp)", fontSize = 14.sp)
                        Text("${settings.iconSizeDp}dp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = settings.iconSizeDp.toFloat(),
                        onValueChange = { onSettingsChanged(settings.copy(iconSizeDp = it.roundToInt())) },
                        valueRange = 44f..68f,
                        steps = 5
                    )

                    // Theme Palette
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Color Theme", fontSize = 14.sp)
                        ThemeDropdown(
                            currentTheme = settings.themeMode,
                            onThemeSelected = { onSettingsChanged(settings.copy(themeMode = it)) }
                        )
                    }

                    // Wallpaper Style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wallpaper Canvas", fontSize = 14.sp)
                        WallpaperDropdown(
                            currentStyle = settings.wallpaperStyle,
                            onStyleSelected = { onSettingsChanged(settings.copy(wallpaperStyle = it)) }
                        )
                    }
                }
            }

            // Section 4: App Vault & Privacy
            SettingsSectionHeader(title = "App Vault & Privacy", icon = Icons.Default.Lock)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hidden Apps PIN Protection", fontSize = 14.sp)
                            Text(
                                if (settings.appLockPin.isNotBlank()) "PIN is currently configured"
                                else "No PIN set (hidden apps open freely)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showPinDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (settings.appLockPin.isNotBlank()) "Change" else "Set PIN")
                        }
                    }
                }
            }

            // Section 5: App Usage Insights
            if (appUsage.isNotEmpty()) {
                SettingsSectionHeader(title = "Digital Insights", icon = Icons.Default.Analytics)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Most Launched Apps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        appUsage.take(4).forEach { usage ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(usage.packageName.substringAfterLast("."), fontSize = 12.sp)
                                Text("${usage.launchCount} launches", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Section 6: Gestures
            SettingsSectionHeader(title = "Gesture Mapping", icon = Icons.Default.TouchApp)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GestureMappingRow(
                        title = "Double Tap",
                        currentAction = settings.doubleTapGesture,
                        onActionChange = { onSettingsChanged(settings.copy(doubleTapGesture = it)) }
                    )
                    GestureMappingRow(
                        title = "Swipe Down",
                        currentAction = settings.swipeDownGesture,
                        onActionChange = { onSettingsChanged(settings.copy(swipeDownGesture = it)) }
                    )
                    GestureMappingRow(
                        title = "Swipe Up",
                        currentAction = settings.swipeUpGesture,
                        onActionChange = { onSettingsChanged(settings.copy(swipeUpGesture = it)) }
                    )
                    GestureMappingRow(
                        title = "Pinch In",
                        currentAction = settings.pinchGesture,
                        onActionChange = { onSettingsChanged(settings.copy(pinchGesture = it)) }
                    )
                }
            }

            // Section 7: Backup & Restore
            SettingsSectionHeader(title = "Data & Backup", icon = Icons.Default.Save)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenBackup,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export / Import Layout JSON")
                    }

                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Layout to Default", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aura Launcher v1.0", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Zero-Jank 120fps • Room Persistence • Modern M3", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure Vault PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter 4-digit PIN for Hidden Apps (leave blank to disable):")
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 6) pinText = it },
                        singleLine = true,
                        label = { Text("PIN") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSettingsChanged(settings.copy(appLockPin = pinText.trim()))
                        showPinDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Home Layout?") },
            text = { Text("This will restore default widgets, dock apps, and home screen grid arrangement.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        showResetConfirm = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun ShapePreviewBox(
    shape: IconShape,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val composeShape = remember(shape) { shape.toComposeShape() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onSelect)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(composeShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = composeShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(shape.displayName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BadgeDropdown(current: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val badges = listOf("DOT", "NUMERIC", "NONE")

    Box {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.clickable { expanded = true }.padding(2.dp)
        ) {
            Text(current, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            badges.forEach { b ->
                DropdownMenuItem(
                    text = { Text(b) },
                    onClick = {
                        onSelected(b)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeDropdown(currentTheme: String, onThemeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val themes = listOf("AMOLED", "NEON", "LIGHT", "SYSTEM")

    Box {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(4.dp)
        ) {
            Text(currentTheme, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            themes.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme) },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WallpaperDropdown(currentStyle: String, onStyleSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val styles = listOf("AURA_GRADIENT", "COSMIC_DARK", "SUNSET", "MINIMAL_BLACK", "SYSTEM")

    Box {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .clickable { expanded = true }
                .padding(4.dp)
        ) {
            Text(
                currentStyle.replace("_", " "),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            styles.forEach { style ->
                DropdownMenuItem(
                    text = { Text(style.replace("_", " ")) },
                    onClick = {
                        onStyleSelected(style)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GestureMappingRow(
    title: String,
    currentAction: GestureAction,
    onActionChange: (GestureAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Box {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(2.dp)
            ) {
                Text(
                    text = currentAction.label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                GestureAction.values().forEach { action ->
                    DropdownMenuItem(
                        text = { Text(action.label) },
                        onClick = {
                            onActionChange(action)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
