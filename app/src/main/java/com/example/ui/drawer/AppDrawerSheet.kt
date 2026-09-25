package com.example.ui.drawer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppUsageEntity
import com.example.engine.IconManager
import com.example.model.AppCategory
import com.example.model.AppInfo
import com.example.model.LauncherSettings
import com.example.ui.home.AppIconView
import kotlinx.coroutines.launch

@Composable
fun AppDrawerSheet(
    isOpen: Boolean,
    apps: List<AppInfo>,
    usageList: List<AppUsageEntity>,
    searchQuery: String,
    selectedCategory: AppCategory,
    settings: LauncherSettings,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (AppCategory) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isOpen) {
        onClose()
    }

    var isVaultUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var drawerDisplayMode by remember { mutableStateOf(settings.drawerLayout) } // "CATEGORIZED", "DETAILED_LIST", "COMPACT"

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        val gridState = rememberLazyGridState()
        val coroutineScope = rememberCoroutineScope()

        // Filter apps based on search query, category, and hidden state
        val filteredApps by remember(apps, searchQuery, selectedCategory, isVaultUnlocked) {
            derivedStateOf {
                apps.filter { app ->
                    val matchesSearch = searchQuery.isBlank() ||
                            app.displayLabel.contains(searchQuery, ignoreCase = true) ||
                            app.packageName.contains(searchQuery, ignoreCase = true)

                    val matchesCategory = when (selectedCategory) {
                        AppCategory.ALL -> !app.isHidden
                        AppCategory.FAVORITES -> app.isFavorite && !app.isHidden
                        AppCategory.HIDDEN -> app.isHidden && isVaultUnlocked
                        else -> app.category == selectedCategory && !app.isHidden
                    }

                    matchesSearch && matchesCategory
                }.sortedBy { it.displayLabel.lowercase() }
            }
        }

        // Top 5 most launched apps
        val topApps by remember(apps, usageList) {
            derivedStateOf {
                val usageMap = usageList.associateBy { it.packageName }
                apps.filter { !it.isHidden }
                    .sortedByDescending { usageMap[it.packageName]?.launchCount ?: 0 }
                    .take(5)
            }
        }

        val alphabetLetters = remember { ('A'..'Z').toList() }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_drawer_sheet"),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            .clickable { onClose() }
                    )
                }

                // Search Bar + Layout Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_search_input"),
                        placeholder = { Text("Search apps & categories...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Drawer style toggle (Grid vs List)
                    IconButton(
                        onClick = {
                            drawerDisplayMode = if (drawerDisplayMode == "DETAILED_LIST") "CATEGORIZED" else "DETAILED_LIST"
                        }
                    ) {
                        Icon(
                            imageVector = if (drawerDisplayMode == "DETAILED_LIST") Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Toggle Layout"
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Top Frequently Used Apps Row (if search is empty and on ALL tab)
                if (searchQuery.isBlank() && selectedCategory == AppCategory.ALL && topApps.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Frequently Used",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            topApps.forEach { app ->
                                AppIconView(
                                    label = app.displayLabel,
                                    packageName = app.packageName,
                                    iconShape = settings.iconShape,
                                    iconSizeDp = 46,
                                    showLabel = true,
                                    badgeMode = settings.badgeMode,
                                    monochrome = settings.monochromeIcons,
                                    labelSize = "SMALL",
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) }
                                )
                            }
                        }
                    }
                }

                // Category Chips Row
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                if (cat == AppCategory.HIDDEN && !isVaultUnlocked && settings.appLockPin.isNotBlank()) {
                                    showPinDialog = true
                                } else {
                                    if (cat == AppCategory.HIDDEN) isVaultUnlocked = true
                                    onCategoryChange(cat)
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (cat == AppCategory.HIDDEN) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = cat.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("drawer_category_${cat.name}")
                        )
                    }
                }

                // Main App Grid or Detailed List
                if (drawerDisplayMode == "DETAILED_LIST") {
                    // Detailed List view
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            DetailedAppRow(
                                app = app,
                                settings = settings,
                                onClick = { onAppClick(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                        }
                    }
                } else {
                    // Grid View
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (filteredApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedCategory == AppCategory.HIDDEN && !isVaultUnlocked)
                                        "App Vault Locked. Enter PIN to view hidden apps."
                                    else if (selectedCategory == AppCategory.HIDDEN)
                                        "No hidden apps in vault."
                                    else "No matching apps found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            val cols = if (drawerDisplayMode == "COMPACT") 5 else settings.gridCols
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(cols),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                items(filteredApps, key = { it.packageName }) { app ->
                                    AppIconView(
                                        label = app.displayLabel,
                                        packageName = app.packageName,
                                        iconShape = settings.iconShape,
                                        iconSizeDp = if (drawerDisplayMode == "COMPACT") settings.iconSizeDp - 6 else settings.iconSizeDp,
                                        showLabel = settings.showLabels,
                                        badgeMode = settings.badgeMode,
                                        monochrome = settings.monochromeIcons,
                                        labelSize = settings.iconLabelSize,
                                        onClick = { onAppClick(app) },
                                        onLongClick = { onAppLongClick(app) }
                                    )
                                }
                            }
                        }

                        // Alphabet index bar
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            alphabetLetters.forEach { letter ->
                                Text(
                                    text = letter.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable {
                                            val targetIdx = filteredApps.indexOfFirst {
                                                it.displayLabel.startsWith(letter, ignoreCase = true)
                                            }
                                            if (targetIdx >= 0) {
                                                coroutineScope.launch {
                                                    gridState.animateScrollToItem(targetIdx)
                                                }
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Vault PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("App Vault PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter 4-digit PIN to access private hidden apps:")
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { if (it.length <= 6) enteredPin = it },
                        singleLine = true,
                        isError = pinError,
                        label = { Text("PIN") }
                    )
                    if (pinError) {
                        Text("Incorrect PIN. Please try again.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPin == settings.appLockPin || settings.appLockPin.isEmpty()) {
                            isVaultUnlocked = true
                            showPinDialog = false
                            onCategoryChange(AppCategory.HIDDEN)
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailedAppRow(
    app: AppInfo,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val iconBitmap = remember(app.packageName) {
        IconManager.getAppIcon(context, app.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(settings.iconShape.toComposeShape())
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = app.displayLabel,
                colorFilter = if (settings.monochromeIcons) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.displayLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${app.category.title} • ${app.packageName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
