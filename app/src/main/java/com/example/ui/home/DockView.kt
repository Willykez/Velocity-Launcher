package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.LauncherItemEntity
import com.example.model.LauncherSettings

@Composable
fun DockView(
    dockItems: List<LauncherItemEntity>,
    settings: LauncherSettings,
    modifier: Modifier = Modifier,
    onItemClick: (LauncherItemEntity) -> Unit,
    onItemLongClick: (LauncherItemEntity) -> Unit,
    onOpenDrawerClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("launcher_dock"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render dock items up to dockCount
            val displayedItems = dockItems.take(settings.dockCount - 1)
            displayedItems.forEach { item ->
                AppIconView(
                    label = item.label,
                    packageName = item.packageName,
                    activityName = item.activityName,
                    iconPackPackage = settings.iconPackPackage,
                    iconShape = settings.iconShape,
                    iconSizeDp = (settings.iconSizeDp - 2).coerceAtLeast(42),
                    showLabel = false,
                    isFolder = item.type == "FOLDER",
                    badgeMode = settings.badgeMode,
                    monochrome = settings.monochromeIcons,
                    labelSize = settings.iconLabelSize,
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) }
                )
            }

            // Always provide App Drawer launcher button in dock
            Box(
                modifier = Modifier
                    .size((settings.iconSizeDp - 2).coerceAtLeast(42).dp)
                    .clip(settings.iconShape.toComposeShape())
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onOpenDrawerClick,
                    modifier = Modifier.testTag("dock_app_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Open App Drawer",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
