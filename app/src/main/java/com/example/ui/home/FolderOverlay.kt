package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.FolderEntity
import com.example.data.LauncherItemEntity
import com.example.model.LauncherSettings

@Composable
fun FolderOverlay(
    folder: FolderEntity,
    itemsInFolder: List<LauncherItemEntity>,
    settings: LauncherSettings,
    onDismiss: () -> Unit,
    onAppClick: (LauncherItemEntity) -> Unit,
    onAppLongClick: (LauncherItemEntity) -> Unit,
    onRenameFolder: (String) -> Unit,
    onDeleteFolder: () -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var folderNameText by remember { mutableStateOf(folder.name) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("folder_overlay_${folder.name}"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = folderNameText,
                            onValueChange = { folderNameText = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = {
                                onRenameFolder(folderNameText.trim().ifEmpty { "Folder" })
                                isEditingName = false
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save Name", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text(
                            text = folder.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { isEditingName = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name", modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Folder apps grid
                if (itemsInFolder.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Empty Folder",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((((itemsInFolder.size / 4 + 1) * 88).coerceAtMost(320)).dp)
                    ) {
                        items(itemsInFolder, key = { it.id }) { item ->
                            AppIconView(
                                label = item.label,
                                packageName = item.packageName,
                                iconShape = settings.iconShape,
                                iconSizeDp = settings.iconSizeDp - 4,
                                showLabel = settings.showLabels,
                                onClick = { onAppClick(item) },
                                onLongClick = { onAppLongClick(item) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDeleteFolder
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete Folder", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
