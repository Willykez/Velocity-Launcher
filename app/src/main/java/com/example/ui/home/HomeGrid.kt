package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LauncherItemEntity
import com.example.data.QuickNoteEntity
import com.example.model.LauncherSettings

@Composable
fun HomeGrid(
    pageIndex: Int,
    items: List<LauncherItemEntity>,
    settings: LauncherSettings,
    isEditing: Boolean,
    quickNote: QuickNoteEntity?,
    onSaveQuickNote: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: (LauncherItemEntity) -> Unit,
    onItemLongClick: (LauncherItemEntity) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onEmptySpaceLongClick: () -> Unit,
    onOpenAddWidget: () -> Unit
) {
    val pageItems = items.filter { it.pageIndex == pageIndex }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onEmptySpaceLongClick()
                    }
                )
            }
            .testTag("home_grid_page_$pageIndex")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // If page 0 and widget enabled, display top At-A-Glance
            if (pageIndex == 0 && settings.showAtAGlanceWidget) {
                AtAGlanceWidget(
                    onSearchClick = onSearchClick
                )
            }

            // Render custom widgets placed on this page
            val widgetItems = pageItems.filter { it.type == "WIDGET" }
            widgetItems.forEach { widgetItem ->
                Box(contentAlignment = Alignment.TopEnd) {
                    when (widgetItem.widgetType) {
                        "SYSTEM_TELEMETRY" -> SystemTelemetryWidget()
                        "QUICK_NOTE" -> QuickNoteWidget(
                            content = quickNote?.content ?: "Tap edit to write a note...",
                            colorHex = quickNote?.colorHex ?: "#6C5CE7",
                            onSaveNote = onSaveQuickNote
                        )
                        "MUSIC_PLAYER" -> MusicControllerWidget()
                    }

                    if (isEditing) {
                        Box(
                            modifier = Modifier
                                .padding(end = 18.dp, top = 8.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                                .clickable { onRemoveItem(widgetItem.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Widget",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (isEditing) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editing Page ${pageIndex + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Button(
                            onClick = onOpenAddWidget,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Widget", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Add Widget", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Grid of app icons and folders
            val gridAppItems = pageItems.filter { it.type != "WIDGET" }

            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.gridCols),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(gridAppItems, key = { it.id }) { item ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        AppIconView(
                            label = item.label,
                            packageName = item.packageName,
                            iconShape = settings.iconShape,
                            iconSizeDp = settings.iconSizeDp,
                            showLabel = settings.showLabels,
                            isFolder = item.type == "FOLDER",
                            badgeMode = settings.badgeMode,
                            monochrome = settings.monochromeIcons,
                            labelSize = settings.iconLabelSize,
                            onClick = { onItemClick(item) },
                            onLongClick = { onItemLongClick(item) }
                        )

                        if (isEditing) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
                                    .clickable { onRemoveItem(item.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
