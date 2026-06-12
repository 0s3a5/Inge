package com.example.myapplication // ⚠️ Cambia por tu paquete si es diferente

import com.google.gson.annotations.SerializedName

data class EventoLocal(
    @SerializedName("punto_id") val puntoId: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("tipo_evento") val tipoEvento: String,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("latitud") val latitud: String,
    @SerializedName("longitud") val longitud: String,
    @SerializedName("creado_por") val creadoPor: String?
)