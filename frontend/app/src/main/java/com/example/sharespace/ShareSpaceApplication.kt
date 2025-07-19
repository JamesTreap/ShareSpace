package com.example.sharespace

import android.app.Application


class ShareSpaceApplication : Application() {
    lateinit var container: ShareSpaceAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultShareSpaceAppContainer(this)
    }
}