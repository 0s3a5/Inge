package com.example.myapplication

data class Catastrofe(
    val id: String,
    val tipoAlerta: String, // Ej: "Alerta Roja", "Simulacro"
    val titulo: String,     // Ej: "Gran Incendio Forestal"
    val ubicacion: String,  // Ej: "Viña del Mar"
    val tiempo: String      // Ej: "Hace 30 min"
)