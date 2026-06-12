package com.example.myapplication // ⚠️ Revisa tu paquete

import com.google.gson.annotations.SerializedName

// ===== CASO 1: REGISTRO =====
data class RegistroRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("rut") val rut: String
)
data class RegistroResponse(val message: String)



// ===== CASO 2: LOGIN =====
// ===== CASO 2: LOGIN (Sigue igual) =====
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val message: String, val usuario: UsuarioData)
data class UsuarioData(val id: String, val nombre: String, val email: String, val tipo_usuario: Int)

// ===== NUEVO: PETICIÓN DE ASCENSO A ORGANIZADOR =====
data class UpgradeRequest(
    @SerializedName("id") val userId: String,
    @SerializedName("num_documento") val numDocumento: String // 👈 Asegúrate de que diga SerializedName
)
data class UpgradeResponse(val message: String)

// ===== CASO 3: CREAR PUNTO DE VOLUNTARIADO =====
data class EventoRequest(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo_evento") val tipoEvento: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("latitud") val latitud: String,
    @SerializedName("longitud") val longitud: String,
    @SerializedName("fecha_evento") val fechaEvento: String, // Formato "2026-06-15T15:30:00Z"
    @SerializedName("creado_por") val creadoPor: String     // Pasamos el UUID guardado
)

data class EventoResponse(
    @SerializedName("message") val message: String
)
data class EventoResponseData(
    @SerializedName("id") val id: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo_evento") val tipoEvento: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("latitud") val latitud: String,
    @SerializedName("longitud") val longitud: String,
    @SerializedName("fecha_evento") val fechaEvento: String,
    @SerializedName("creado_por") val creadoPor: String
)