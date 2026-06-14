package com.example.myapplication // Asegúrate de que este sea tu paquete real
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// ==========================================================
// 1. DEFINICIÓN DE LAS RUTAS DEL BACKEND (INTERFAZ)
// ==========================================================
interface ApiService {

    // 📝 Ruta de Registro
    @POST("api/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): RegistroResponse

    // 🔑 Ruta de Login
    @POST("api/login")
    suspend fun iniciarSesion(@Body request: LoginRequest): LoginResponse

    // 🚀 Ruta de Creación de Eventos (Dejamos solo una con el nombre correcto)
    @POST("api/eventos")
    suspend fun crearEvento(@Body request: EventoRequest): EventoResponse

    // 🗺️ Ruta para obtener los eventos y pintarlos en el mapa
    @GET("api/eventos")
    suspend fun obtenerEventos(): List<EventoResponseData>

    // 🪪 Ruta para subir el documento y subir de rango
    @POST("api/upgrade")
    suspend fun ascenderAOrganizador(@Body request: UpgradeRequest): UpgradeResponse
}

// ==========================================================
// 2. CONFIGURACIÓN DEL CLIENTE RETROFIT
// ==========================================================
object RetrofitClient {
    private const val BASE_URL = "https://api-voluntariados.onrender.com/" // 🚨 RECUERDA: Debe terminar con "/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
