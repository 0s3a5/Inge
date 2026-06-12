package com.example.myapplication // ⚠️ Revisa que sea tu paquete real

import android.content.Context

// 🚨 Es vital que lleve 'val' antes de 'context'
class SharedPrefs(val context: Context) {

    private val prefs = context.getSharedPreferences("NeonPrefs", Context.MODE_PRIVATE)

    var userId: String?
        get() = prefs.getString("userId", null)
        set(value) = prefs.edit().putString("userId", value).apply()

    var tipoUsuario: Int
        get() = prefs.getInt("tipoUsuario", 0)
        set(value) = prefs.edit().putInt("tipoUsuario", value).apply()

    var username: String?
        get() = prefs.getString("username", null)
        set(value) = prefs.edit().putString("username", value).apply()

    fun borrarSesion() {
        prefs.edit().clear().apply()
    }
}