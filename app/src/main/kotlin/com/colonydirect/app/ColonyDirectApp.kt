package com.colonydirect.app

import android.app.Application

class ColonyDirectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
