package com.example.myapplication // ⚠️ Revisa que sea tu paquete real

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_perfil) // 👈 Vincula el XML del Paso 1

        // 🔍 Al estar en una Activity, se buscan directo después del setContentView
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        btnIngresar.setOnClickListener {
            val correo = etEmail.text.toString().trim()
            val clave = etPassword.text.toString().trim()

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    // Enviamos la petición estructurada del Caso 2
                    val respuesta = RetrofitClient.api.iniciarSesion(LoginRequest(correo, clave))

                    // 🚨 REPARACIÓN DE 'USUARIO':
                    // 'respuesta' es un LoginResponse, por ende tiene '.usuario' dentro
                    val idUsuarioConectado = respuesta.usuario.id
                    val nombreUsuario = respuesta.usuario.nombre

                    // Guardamos el UUID en nuestras SharedPreferences globales
                    MyApp.prefs.userId = idUsuarioConectado
                    MyApp.prefs.username = nombreUsuario
                    MyApp.prefs.tipoUsuario = respuesta.usuario.tipo_usuario

                    Toast.makeText(this@LoginActivity, "¡Bienvenido $nombreUsuario!", Toast.LENGTH_SHORT).show()

                    // Redirigimos a la MainActivity (donde está tu mapa)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}