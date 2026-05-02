package com.alejandrosahonero.courthub

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alejandrosahonero.courthub.di.AppContainer
import com.alejandrosahonero.courthub.utils.NotificationHelper
import com.alejandrosahonero.courthub.utils.ReservationReminderWorker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import java.util.concurrent.TimeUnit

class CourtHubApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Activa caché offline nativa de Firestore
        val settings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {})
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings

        container = AppContainer(this)

        NotificationHelper.createChannel(this)
        scheduleReminderWorker()
    }

    private fun scheduleReminderWorker() {
        val request = OneTimeWorkRequestBuilder<ReservationReminderWorker>()
            .setInitialDelay(2, TimeUnit.SECONDS) // espera 2s a que la app cargue
            .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "reservation_reminder",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
