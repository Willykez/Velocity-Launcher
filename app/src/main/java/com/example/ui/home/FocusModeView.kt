package com.example.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusModeView(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onExitFocusMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("focus_mode_view"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with Clock and Exit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = currentTime,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = currentDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                IconButton(onClick = onExitFocusMode) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Focus Mode", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Typographic List of essentials
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val focusApps = apps.filter { !it.isHidden }.take(7)
            items(focusApps, key = { it.packageName }) { app ->
                Text(
                    text = app.displayLabel.lowercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app) }
                        .padding(vertical = 4.dp)
                )
            }
        }

        // Bottom zen note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = " Minimalist Mode Active • Stay Intentional",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
