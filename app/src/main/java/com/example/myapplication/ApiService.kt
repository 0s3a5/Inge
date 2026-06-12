package com.example.myapplication // ⚠️ Cambia por tu paquete real si es distinto

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
// 1. Definimos las rutas del backend en la nube
interface ApiService {
    // Apunta a la ruta de producción: https://tu-app.onrender.com/api/eventos
    @POST("api/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): RegistroResponse

    // 🔑 Cambiado para usar el formato Caso 2 (Este reparará el error de .usuario)
    @POST("api/login")
    suspend fun iniciarSesion(@Body request: LoginRequest): LoginResponse

    // 🗺️ Formato Caso 3
    @POST("api/eventos")
    suspend fun crearPuntoVoluntariado(@Body request: EventoRequest): EventoResponse
    @GET("api/eventos")
    suspend fun obtenerEventos(): List<EventoResponseData> // 👈 Esto quitará el error en tu fragmento
    @POST("api/upgrade")
    suspend fun ascenderAOrganizador(@Body request: UpgradeRequest): UpgradeResponse
}

// 2. Configuramos la conexión con la URL pública de Render
object RetrofitClient {
    // ⚠️ REEMPLAZA esta URL de ejemplo por el enlace real que te dé Render al desplegar
    // MUY IMPORTANTE: Asegúrate de que termine con una barra diagonal "/"
    private const val BASE_URL = "https://api-voluntariados.onrender.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Traduce automáticamente el JSON a objetos Kotlin
            .build()
            .create(ApiService::class.java)
    }
}
