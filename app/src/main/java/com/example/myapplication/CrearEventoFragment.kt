package com.example.myapplication

import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class CrearEventoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_evento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTitulo = view.findViewById<EditText>(R.id.etEvTitulo)
        val etDescripcion = view.findViewById<EditText>(R.id.etEvDescripcion)
        val etTipo = view.findViewById<EditText>(R.id.etEvTipo)
        val etDireccion = view.findViewById<EditText>(R.id.etEvDireccion)
        val etFecha = view.findViewById<EditText>(R.id.etEvFecha)
        val etLat = view.findViewById<EditText>(R.id.etEvLat)
        val etLng = view.findViewById<EditText>(R.id.etEvLng)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarEvento)

        // 🚀 ESCUCHADOR AUTOMÁTICO: Cuando el usuario deja de escribir en la dirección
        etDireccion.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // Si el usuario salió de la casilla de dirección...
                val direccionEscrita = etDireccion.text.toString().trim()
                if (direccionEscrita.isNotEmpty()) {
                    // Buscamos las coordenadas en un hilo secundario para no congelar la pantalla
                    obtenerCoordenadasDesdeDireccion(direccionEscrita, etLat, etLng)
                }
            }
        }

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val desc = etDescripcion.text.toString().trim()
            val tipo = etTipo.text.toString().trim()
            val dir = etDireccion.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val latStr = etLat.text.toString().trim()
            val lngStr = etLng.text.toString().trim()

            val organizadorId = MyApp.prefs.userId
            android.util.Log.d("DEBUG_EVENTO", "El ID del organizador recuperado es: $organizadorId")

            if (organizadorId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Error interno: No se detectó tu ID de usuario. Reincia sesión.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (titulo.isEmpty() || desc.isEmpty() || tipo.isEmpty() || dir.isEmpty() || fecha.isEmpty() || latStr.isEmpty() || lngStr.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val request = EventoRequest(
                        id = "0",
                        titulo = titulo,
                        descripcion = desc,
                        tipoEvento = tipo,      // 🚨 Asegúrate que diga 'tipoEvento' (en CamelCase)
                        direccion = dir,
                        latitud = latStr.toDoubleOrNull() ?: 0.0,   // 🚨 Forzamos conversión a número decimal
                        longitud = lngStr.toDoubleOrNull() ?: 0.0, // 🚨 Forzamos conversión a número decimal
                        fechaEvento = fecha,    // 🚨 Asegúrate que diga 'fechaEvento'
                        creadoPor = organizadorId!!
                    )

                    val respuesta = RetrofitClient.api.crearEvento(request)
                    Toast.makeText(requireContext(), respuesta.message, Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al subir el evento al servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 🗺️ FUNCIÓN INTERNA QUE CONVIERTE TEXTO A COORDENADAS (GEOCODING)
    private fun obtenerCoordenadasDesdeDireccion(direccion: String, etLat: EditText, etLng: EditText) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Buscamos la dirección en los servidores de Google (retorna una lista de posibles direcciones)
                val listaDirecciones = geocoder.getFromLocationName(direccion, 1)

                if (!listaDirecciones.isNullOrEmpty()) {
                    val ubicacionEncontrada = listaDirecciones[0]
                    val latitudObtenida = ubicacionEncontrada.latitude
                    val longitudObtenida = ubicacionEncontrada.longitude

                    // Volvemos al hilo principal para pintar los datos en las cajitas de texto
                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.Main) {
                            etLat.setText(latitudObtenida.toString())
                            etLng.setText(longitudObtenida.toString()) // 🚀 ¡ESTA LÍNEA asigna la longitud calculada!
                            Toast.makeText(
                                requireContext(),
                                "📍 Coordenadas ubicadas con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                        }}
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No se encontró la dirección exacta. Intenta añadir la comuna o ciudad.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error de red al buscar la dirección", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun validarRutChileno(rut: String): Boolean {
        // 1. Limpiamos el texto: quitamos puntos, guiones, espacios y lo pasamos a Mayúsculas (por si hay una 'k')
        val rutLimpio = rut.replace(".", "")
            .replace("-", "")
            .trim()
            .uppercase()

        // 2. Un RUT chileno real con su dígito verificador tiene entre 8 y 9 caracteres en total
        if (rutLimpio.length < 8 || rutLimpio.length > 9) return false

        return try {
            // Separámos el cuerpo del dígito verificador (DV)
            val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
            val dvIngresado = rutLimpio.last()

            // Si el cuerpo contiene letras, el RUT es inválido de inmediato
            val cuerpoEntero = cuerpo.toIntOrNull() ?: return false

            // 3. Comenzamos el cálculo matemático del Módulo 11
            var suma = 0
            var multiplicador = 2
            var auxCuerpo = cuerpoEntero

            while (auxCuerpo > 0) {
                val ultimoDigito = auxCuerpo % 10
                suma += ultimoDigito * multiplicador

                // El multiplicador va ciclando de 2 a 7
                multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
                auxCuerpo /= 10
            }

            val resto = suma % 11
            val resultadoDiferencia = 11 - resto

            // Determinamos cuál debería ser el Dígito Verificador esperado
            val dvEsperado = when (resultadoDiferencia) {
                11 -> '0'
                10 -> 'K'
                else -> resultadoDiferencia.toString()[0]
            }

            // 4. Retorna 'true' si el DV ingresado coincide con el calculado por la app
            dvIngresado == dvEsperado

        } catch (e: Exception) {
            false
        }
    }
}