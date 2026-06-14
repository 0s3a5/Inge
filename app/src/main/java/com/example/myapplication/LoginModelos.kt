package com.example.myapplication // ⚠️ Revisa tu paquete

import com.google.gson.annotations.SerializedName

// ===== CASO 1: REGISTRO =====
data class RegistroRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("rut") val rut: String,
    @SerializedName("num_documento") val numDocumento: String = "0"
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
    val id: String,
    val titulo: String,
    val descripcion: String,

    @SerializedName("tipo_evento") val tipoEvento: String, // Traspasa tipoEvento a tipo_evento
    val direccion: String,
    val latitud: Double,
    val longitud: Double,

    @SerializedName("fecha_evento") val fechaEvento: String, // Traspasa fechaEvento a fecha_evento
    @SerializedName("creado_por") val creadoPor: String     // Traspasa creadoPor a creado_por
)
data class EventoResponse(
    @SerializedName("message") val message: String
)

data class EventoResponseData(
    @SerializedName("evento_id") val evento_id: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo_evento") val tipo_evento: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("latitud") val latitud: String,
    @SerializedName("longitud") val longitud: String,
    @SerializedName("fecha_evento") val fecha_evento: String,
    @SerializedName("creado_por") val creado_por: String,
    @SerializedName("nombre_creador") val nombre_creador: String
)
