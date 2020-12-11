package com.skysam.hchirinos.myfinances.common

import android.app.Application
import android.content.Context

class MyFinancesApp: Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    object MyFinancesAppObject {
        fun getContext(): Context {
            return appContext
        }
    }
}