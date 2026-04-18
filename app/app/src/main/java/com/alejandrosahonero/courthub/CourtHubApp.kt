package com.alejandrosahonero.courthub

import android.app.Application
import com.alejandrosahonero.courthub.di.AppContainer

class CourtHubApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}