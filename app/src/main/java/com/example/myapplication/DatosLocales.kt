package com.example.myapplication

object DatosLocales {
    val listaVoluntariados = listOf(
        Voluntariado("1", "Hospital Carlos van Buren", "Apoyo en Admisión", "Valparaíso", "+18 años", "Estatal", -33.0475, -71.6122),
        Voluntariado("2", "Organización Vecinal Reñaca", "Recolección", "Viña del Mar", "Fines de semana", "ONG", -33.0115, -71.5458),
        Voluntariado("3", "Bingo Solidario", "Ayuda en mesas", "Quilpué", "Ninguno", "Personal", -33.0480, -71.4425)
    )
    val listaCatastrofes = listOf(
        Catastrofe("1", "Alerta Roja", "Gran Incendio Forestal", "Viña del Mar", "Hace 30 min"),
        Catastrofe("2", "Alerta Amarilla", "Desborde de Canal", "Quilpué", "Hace 2 horas"),
        Catastrofe("3", "Simulacro", "Inundación Río Maipo", "San Antonio", "Hace 1 día")
    )
}