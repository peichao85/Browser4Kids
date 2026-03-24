package com.browser4kids

import android.app.Application

class Browser4KidsApplication : Application() {

    lateinit var database: com.browser4kids.data.database.AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = com.browser4kids.data.database.AppDatabase.getInstance(this)
    }
}
