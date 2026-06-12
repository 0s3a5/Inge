package com.example.myapplication

import android.app.Application

class MyApp : Application() {
    companion object {
        lateinit var prefs: SharedPrefs
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // 🚀 Aquí le pasamos el contexto global seguro de la aplicación
        prefs = SharedPrefs(applicationContext)
    }
}