package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickNoteWidget(
    content: String,
    colorHex: String,
    onSaveNote: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var noteText by remember(content) { mutableStateOf(content) }
    var selectedColor by remember(colorHex) { mutableStateOf(colorHex) }

    val noteColors = listOf("#6C5CE7", "#00CEC9", "#FF7675", "#FDCB6E", "#00B894")
    val parsedColor = runCatching { Color(android.graphics.Color.parseColor(selectedColor)) }.getOrDefault(Color(0xFF6C5CE7))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("quick_note_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = parsedColor.copy(alpha = 0.18f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, parsedColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NoteAlt,
                        contentDescription = null,
                        tint = parsedColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Quick Note",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isEditing) {
                    IconButton(
                        onClick = {
                            onSaveNote(noteText, selectedColor)
                            isEditing = false
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note", tint = parsedColor)
                    }
                } else {
                    IconButton(
                        onClick = { isEditing = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // Color choices
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    noteColors.forEach { hex ->
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == hex) 2.dp else 0.dp,
                                    color = if (selectedColor == hex) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
            } else {
                Text(
                    text = if (noteText.isNotBlank()) noteText else "Tap edit to write a note...",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    modifier = Modifier.clickable { isEditing = true }
                )
            }
        }
    }
}
