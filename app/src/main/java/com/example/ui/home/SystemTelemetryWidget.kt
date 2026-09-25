package com.example.ui.home

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SystemTelemetryWidget(
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val (storageUsedGB, storageTotalGB, storageRatio) = remember {
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = totalBytes - availableBytes
            val totalGB = (totalBytes / (1024 * 1024 * 1024)).toInt()
            val usedGB = (usedBytes / (1024 * 1024 * 1024)).toInt()
            val ratio = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0.5f
            Triple(usedGB, totalGB, ratio.coerceIn(0f, 1f))
        } catch (_: Exception) {
            Triple(48, 128, 0.38f)
        }
    }

    val batteryPct = remember {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("system_telemetry_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "System Telemetry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Aura Engine • Live",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Storage bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Internal Storage ($storageUsedGB GB / $storageTotalGB GB)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(storageRatio * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LinearProgressIndicator(
                    progress = { storageRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round
                )
            }

            // Quick Hardware Shortcut Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickShortcutButton(
                    icon = Icons.Default.Wifi,
                    label = "Wi-Fi",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) {}
                    }
                )
                QuickShortcutButton(
                    icon = Icons.Default.Bluetooth,
                    label = "Bluetooth",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) {}
                    }
                )
                QuickShortcutButton(
                    icon = Icons.Default.BrightnessMedium,
                    label = "Display",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) {}
                    }
                )
                QuickShortcutButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
