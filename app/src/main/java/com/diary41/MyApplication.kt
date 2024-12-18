package com.diary41

import android.app.Application
import com.github.anrwatchdog.ANRWatchDog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        //MultiDex.install(this)
        ANRWatchDog().start()
    }
}