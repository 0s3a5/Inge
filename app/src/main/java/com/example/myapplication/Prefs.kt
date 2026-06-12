package com.example.myapplication

import android.content.Context

class Prefs(context: Context) {
    private val storage = context.getSharedPreferences("SesionUsuario", Context.MODE_PRIVATE)

    // Guarda el tipo de usuario (0 por defecto si no está logueado)
    var tipoUsuario: Int
        get() = storage.getInt("tipo_usuario", 0)
        set(value) = storage.edit().putInt("tipo_usuario", value).apply()

    var username: String?
        get() = storage.getString("username", "")
        set(value) = storage.edit().putString("username", value).apply()

    // Limpiar sesión al hacer Logout
    fun borrarSesion() {
        storage.edit().clear().apply()
    }
}