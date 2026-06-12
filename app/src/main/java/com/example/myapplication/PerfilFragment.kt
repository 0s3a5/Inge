package com.example.myapplication // ⚠️ REVISA: Asegúrate de que este sea el nombre real de tu paquete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el diseño de la pantalla de perfil
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ==========================================================
        // 🏗️ 1. REFERENCIAS DE LAS VISTAS (CONTENEDORES)
        // ==========================================================
        val vistaLogin = view.findViewById<LinearLayout>(R.id.vistaLogin)
        val vistaRegistro = view.findViewById<LinearLayout>(R.id.vistaRegistro)
        val vistaPerfilLogueado = view.findViewById<LinearLayout>(R.id.vistaPerfilLogueado)
        val seccionUpgrade = view.findViewById<LinearLayout>(R.id.seccionUpgrade)

        // ==========================================================
        // 📧 2. COMPONENTES DEL FORMULARIO DE LOGIN (Caso 2 JSON)
        // ==========================================================
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnIngresar = view.findViewById<Button>(R.id.btnIngresar)
        val tvIrARegistro = view.findViewById<TextView>(R.id.tvIrARegistro)

        // ==========================================================
        // 📝 3. COMPONENTES DEL FORMULARIO DE REGISTRO (Caso 1 JSON)
        // ==========================================================
        val etRegNombre = view.findViewById<EditText>(R.id.etRegistroNombre)
        val etRegEmail = view.findViewById<EditText>(R.id.etRegistroEmail)
        val etRegPassword = view.findViewById<EditText>(R.id.etRegistroPassword)
        val etRegRut = view.findViewById<EditText>(R.id.etRegistroRut)
        val btnRegistrar = view.findViewById<Button>(R.id.btnRegistrar)
        val tvIrALogin = view.findViewById<TextView>(R.id.tvIrALogin)

        // ==========================================================
        // 🚀 4. COMPONENTES DEL UPGRADE POSTERIOR
        // ==========================================================
        val etDocUpgrade = view.findViewById<EditText>(R.id.etDocUpgrade)
        val btnSolicitarUpgrade = view.findViewById<Button>(R.id.btnSolicitarUpgrade)

        // ==========================================================
        // 👤 5. COMPONENTES DEL PERFIL ACTIVO
        // ==========================================================
        val tvNombreUsuario = view.findViewById<TextView>(R.id.tvNombreUsuario)
        val tvRolUsuario = view.findViewById<TextView>(R.id.tvRolUsuario)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)

        // ==========================================================
        // 🔄 6. CONTROLADOR DE ESTADOS DE LA INTERFAZ
        // ==========================================================
        fun evaluarEstadoInterfaz() {
            val idUsuario = MyApp.prefs.userId

            if (idUsuario != null) {
                // Usuario logueado: Mostramos perfil, ocultamos formularios de acceso
                vistaLogin?.visibility = View.GONE
                vistaRegistro?.visibility = View.GONE
                vistaPerfilLogueado?.visibility = View.VISIBLE

                tvNombreUsuario?.text = MyApp.prefs.username ?: "Usuario"
                val rol = MyApp.prefs.tipoUsuario

                if (rol == 2) {
                    tvRolUsuario?.text = "Rol: Organizador (Puedes publicar eventos)"
                    seccionUpgrade?.visibility = View.GONE // Ya es organizador, ocultamos el formulario
                } else {
                    tvRolUsuario?.text = "Rol: Voluntario Común"
                    seccionUpgrade?.visibility = View.VISIBLE // Aún puede subir de nivel
                }
            } else {
                // Sin sesión: Mostramos login, ocultamos el resto
                vistaLogin?.visibility = View.VISIBLE
                vistaRegistro?.visibility = View.GONE
                vistaPerfilLogueado?.visibility = View.GONE
            }
        }

        // Ejecución inicial al abrir la pestaña
        evaluarEstadoInterfaz()

        // ==========================================================
        // 🔀 7. NAVEGACIÓN ENTRE FORMULARIOS (LOGIN <-> REGISTRO)
        // ==========================================================
        tvIrARegistro?.setOnClickListener {
            vistaLogin?.visibility = View.GONE
            vistaRegistro?.visibility = View.VISIBLE
        }

        tvIrALogin?.setOnClickListener {
            vistaRegistro?.visibility = View.GONE
            vistaLogin?.visibility = View.VISIBLE
        }

        // ==========================================================
        // 🔑 8. ACCIÓN: ENVIAR LOGIN (Caso 2 JSON)
        // ==========================================================
        btnIngresar?.setOnClickListener {
            val correo = etEmail?.text.toString().trim()
            val clave = etPassword?.text.toString().trim()

            if (correo.isEmpty() || clave.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa los campos de ingreso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val respuesta = RetrofitClient.api.iniciarSesion(LoginRequest(correo, clave))

                    // Salvamos la sesión en el almacenamiento local seguro
                    MyApp.prefs.userId = respuesta.usuario.id
                    MyApp.prefs.username = respuesta.usuario.nombre
                    MyApp.prefs.tipoUsuario = respuesta.usuario.tipo_usuario

                    Toast.makeText(requireContext(), "¡Bienvenido de vuelta, ${respuesta.usuario.nombre}!", Toast.LENGTH_SHORT).show()
                    evaluarEstadoInterfaz()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: Credenciales incorrectas", Toast.LENGTH_LONG).show()
                }
            }
        }

        // ==========================================================
        // 📝 9. ACCIÓN: ENVIAR REGISTRO SIMPLIFICADO (Caso 1 JSON)
        // ==========================================================
        btnRegistrar?.setOnClickListener {
            val nombre = etRegNombre?.text.toString().trim()
            val email = etRegEmail?.text.toString().trim()
            val password = etRegPassword?.text.toString().trim()
            val rut = etRegRut?.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || rut.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val objetoRequest = RegistroRequest(nombre, email, password, rut)
                    val respuesta = RetrofitClient.api.registrarUsuario(objetoRequest)

                    Toast.makeText(requireContext(), respuesta.message, Toast.LENGTH_LONG).show()

                    // Limpieza de campos y regreso al Login
                    etRegNombre.text.clear()
                    etRegEmail.text.clear()
                    etRegPassword.text.clear()
                    etRegRut.text.clear()

                    vistaRegistro?.visibility = View.GONE
                    vistaLogin?.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al procesar el registro en el servidor", Toast.LENGTH_LONG).show()
                }
            }
        }

        // ==========================================================
        // 🚀 10. ACCIÓN: SOLICITAR ASCENSO A ORGANIZADOR (POST-LOGIN)
        // ==========================================================
        btnSolicitarUpgrade?.setOnClickListener {
            val numDocumento = etDocUpgrade?.text.toString().trim()
            val uid = MyApp.prefs.userId

            if (numDocumento.isEmpty() || uid == null) {
                Toast.makeText(requireContext(), "Ingresa tu número de documento de identidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Enviamos el UUID guardado y el documento ingresado posteriormente
                    val respuesta = RetrofitClient.api.ascenderAOrganizador(UpgradeRequest(uid, numDocumento))

                    // Modificamos localmente el rol del usuario a Organizador (Tipo 2)
                    MyApp.prefs.tipoUsuario = 2

                    Toast.makeText(requireContext(), respuesta.message, Toast.LENGTH_LONG).show()
                    etDocUpgrade.text.clear()
                    evaluarEstadoInterfaz() // Refresca y quita el cuadro de upgrade
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al validar documento con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ==========================================================
        // 🚪 11. ACCIÓN: CERRAR SESIÓN
        // ==========================================================
        btnCerrarSesion?.setOnClickListener {
            MyApp.prefs.borrarSesion()
            Toast.makeText(requireContext(), "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()

            // Limpieza de inputs de login previos
            etEmail?.text?.clear()
            etPassword?.text?.clear()

            evaluarEstadoInterfaz()
        }
    }
}