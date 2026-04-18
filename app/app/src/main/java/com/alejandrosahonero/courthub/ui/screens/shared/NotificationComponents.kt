package com.alejandrosahonero.courthub.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alejandrosahonero.courthub.domain.model.AppNotification
import com.alejandrosahonero.courthub.domain.model.NotificationType
import com.alejandrosahonero.courthub.ui.theme.Error
import com.alejandrosahonero.courthub.ui.theme.Red600
import com.alejandrosahonero.courthub.ui.theme.Success
import com.alejandrosahonero.courthub.ui.theme.SurfaceVariant
import com.alejandrosahonero.courthub.ui.theme.TextHint
import com.alejandrosahonero.courthub.ui.theme.Warning

@Composable
fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val (icon, iconColor) = when (notification.type) {
        NotificationType.RESERVATION_CONFIRMED -> Icons.Default.CheckCircle to Success
        NotificationType.CANCELLATION -> Icons.Default.Cancel to Error
        NotificationType.REMINDER -> Icons.Default.Schedule to Warning
        NotificationType.MAINTENANCE -> Icons.Default.Warning to Warning
        NotificationType.PAYMENT_RECEIVED -> Icons.Default.Payments to Success
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) SurfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                formatRelativeTime(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = TextHint
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Red600)
            )
        }
    }
}

fun formatRelativeTime(epochMillis: Long): String {
    val diff = System.currentTimeMillis() - epochMillis
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "Ahora mismo"
        minutes < 60 -> "Hace $minutes min"
        hours < 24 -> "Hace $hours h"
        days == 1L -> "Ayer"
        else -> "Hace $days días"
    }
}