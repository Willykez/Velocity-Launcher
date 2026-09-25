package com.example.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun ItemContextMenu(
    title: String,
    packageName: String?,
    isHomeItem: Boolean,
    isFavorite: Boolean,
    isHidden: Boolean,
    onDismiss: () -> Unit,
    onAddToHome: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onToggleHidden: (() -> Unit)? = null,
    onRename: () -> Unit,
    onAppInfo: () -> Unit,
    onRemoveFromHome: (() -> Unit)? = null,
    onUninstall: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("item_context_menu"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!packageName.isNullOrBlank()) {
                        Text(
                            text = packageName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (!isHomeItem && onAddToHome != null) {
                    ContextMenuItemRow(
                        icon = Icons.Default.AddHome,
                        title = "Add to Home Screen",
                        onClick = onAddToHome
                    )
                }

                if (onToggleFavorite != null) {
                    ContextMenuItemRow(
                        icon = if (isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                        title = if (isFavorite) "Remove from Favorites" else "Mark as Favorite",
                        onClick = onToggleFavorite
                    )
                }

                ContextMenuItemRow(
                    icon = Icons.Default.Edit,
                    title = "Rename Label",
                    onClick = onRename
                )

                if (onToggleHidden != null) {
                    ContextMenuItemRow(
                        icon = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        title = if (isHidden) "Unhide App" else "Hide from Drawer",
                        onClick = onToggleHidden
                    )
                }

                ContextMenuItemRow(
                    icon = Icons.Default.Info,
                    title = "App Info",
                    onClick = onAppInfo
                )

                if (isHomeItem && onRemoveFromHome != null) {
                    ContextMenuItemRow(
                        icon = Icons.Default.Delete,
                        title = "Remove from Home",
                        onClick = onRemoveFromHome
                    )
                }

                ContextMenuItemRow(
                    icon = Icons.Default.Delete,
                    title = "Uninstall App",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onUninstall
                )
            }
        }
    }
}

@Composable
private fun ContextMenuItemRow(
    icon: ImageVector,
    title: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}
