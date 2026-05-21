package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class PerfilFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Vincular los contenedores
        val layoutLogin = view.findViewById<LinearLayout>(R.id.layoutLogin)
        val layoutPerfil = view.findViewById<LinearLayout>(R.id.layoutPerfil)

        // 2. Vincular elementos del Login
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        // 3. Vincular elementos del Perfil
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // DATOS LOCALES (Simulando una base de datos)
        val emailCorrecto = "admin@test.com"
        val passwordCorrecta = "1234"

        // Lógica del botón INGRESO
        btnLogin.setOnClickListener {
            val emailIngresado = etEmail.text.toString()
            val passwordIngresada = etPassword.text.toString()

            if (emailIngresado == emailCorrecto && passwordIngresada == passwordCorrecta) {
                // Éxito: Ocultamos el login y mostramos el perfil
                layoutLogin.visibility = View.GONE
                layoutPerfil.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show()
            } else {
                // Error en los datos
                Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }

        // Lógica del botón CERRAR SESIÓN
        btnLogout.setOnClickListener {
            // Limpiamos las cajas de texto
            etEmail.text.clear()
            etPassword.text.clear()

            // Volvemos a mostrar el login y ocultar el perfil
            layoutPerfil.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }
    }
}