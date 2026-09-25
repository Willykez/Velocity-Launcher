package com.example.ui.home

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.IconManager
import com.example.model.IconShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconView(
    label: String,
    packageName: String?,
    iconShape: IconShape,
    iconSizeDp: Int,
    showLabel: Boolean,
    isFolder: Boolean = false,
    badgeMode: String = "DOT", // NONE, DOT, NUMERIC
    monochrome: Boolean = false,
    labelSize: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val composeShape = remember(iconShape) { iconShape.toComposeShape() }

    val labelSp = when (labelSize) {
        "SMALL" -> 10.sp
        "LARGE" -> 14.sp
        else -> 12.sp
    }

    // Hashcode-based simulated unread badge for demonstration of notification dot
    val hasBadge = remember(packageName) {
        packageName != null && (packageName.hashCode() % 3 == 0)
    }
    val badgeNumber = remember(packageName) {
        if (packageName != null) (packageName.hashCode() % 5 + 1).coerceAtLeast(1) else 1
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width((iconSizeDp + 26).dp)
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("app_icon_$label")
    ) {
        Box(
            modifier = Modifier.size(iconSizeDp.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(elevation = 2.dp, shape = composeShape)
                    .clip(composeShape)
                    .background(
                        if (isFolder) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isFolder) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size((iconSizeDp * 0.65f).dp)
                    )
                } else if (!packageName.isNullOrBlank()) {
                    val iconBitmap: Bitmap = remember(packageName) {
                        IconManager.getAppIcon(context, packageName)
                    }
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentDescription = label,
                        colorFilter = if (monochrome) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size((iconSizeDp * 0.6f).dp)
                    )
                }
            }

            // Notification Badge overlay
            if (hasBadge && !isFolder && badgeMode != "NONE") {
                if (badgeMode == "DOT") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                } else if (badgeMode == "NUMERIC") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeNumber.toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = labelSp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
