package com.alejandrosahonero.courthub.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "courthub_reminders"
    const val CHANNEL_NAME = "Recordatorios de reserva"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de recordatorio de reservas"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showReminderNotification(
        context: Context,
        courtName: String,
        date: String,
        startTime: String,
        endTime: String
    ) {
        showNotification(
            context,
            "Recordatorio de Reserva",
            "Tienes una reserva en $courtName el $date de $startTime a $endTime"
        )
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
