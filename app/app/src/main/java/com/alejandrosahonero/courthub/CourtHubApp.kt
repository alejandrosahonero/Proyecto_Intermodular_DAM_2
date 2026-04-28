package com.alejandrosahonero.courthub

import android.app.Application
import com.alejandrosahonero.courthub.di.AppContainer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings

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
    }
}