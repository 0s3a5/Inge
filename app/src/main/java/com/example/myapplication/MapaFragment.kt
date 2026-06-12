package com.example.myapplication // ⚠️ REVISA: Asegúrate de que coincida con tu paquete real

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var marcadorUsuario: Marker? = null
    private var rastreoActivo = false

    // Lanzador del diálogo de permisos del sistema de Android
    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            iniciarRastreo()
            rastreoActivo = true
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            Log.w("GPS", "El usuario denegó el permiso de ubicación.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el diseño con tu barra de búsqueda y botones
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos el cliente de ubicación de Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Configuración del comportamiento del GPS en tiempo real
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                val ubicacion = resultado.lastLocation ?: return
                val posicion = LatLng(ubicacion.latitude, ubicacion.longitude)

                // Si no existe el pin azul del usuario, lo crea; si existe, lo mueve sutilmente
                if (marcadorUsuario == null) {
                    marcadorUsuario = mMap.addMarker(
                        MarkerOptions()
                            .position(posicion)
                            .title("Tú estás aquí")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                } else {
                    marcadorUsuario?.position = posicion
                }

                // Si el rastreo dinámico está activo, la cámara sigue los pasos del usuario
                if (rastreoActivo) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16f))
                }

                Log.d("GPS", "Ubicación actualizada: ${ubicacion.latitude}, ${ubicacion.longitude}")
            }
        }

        // 1. Inicializamos Google Maps de fondo
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // 2. Botón de ubicación manual (Centra el mapa rápido en tus coordenadas de desarrollo)
        val btnUbicacionManual = view.findViewById<FloatingActionButton>(R.id.btnUbicacionManual)
        btnUbicacionManual?.setOnClickListener {
            val miZonaPruebas = LatLng(-33.456789, -70.654321) // ⚠️ Cambia por las tuyas si deseas
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miZonaPruebas, 14f))
            Log.d("BOTON_CLICK", "Moviendo cámara a zona de pruebas manual.")
        }

        // 3. Botón GPS — Activa y desactiva el rastreo dinámico en el mapa
        val btnGPS = view.findViewById<FloatingActionButton>(R.id.btnGPS)
        btnGPS?.setOnClickListener {
            if (rastreoActivo) {
                detenerRastreo()
                rastreoActivo = false
                Log.d("GPS", "Rastreo detenido de forma manual.")
            } else {
                verificarPermisoEIniciar()
                Log.d("GPS", "Iniciando peticiones de rastreo...")
            }
        }

        // 4. 🔐 CONTROL DE ACCESO POR ROLES (Tipo de usuario)
        // Buscamos tu botón flotante o de interfaz para añadir voluntariados
        val btnAgregarVoluntariado = view.findViewById<FloatingActionButton>(R.id.btnAgregarVoluntariado) // ⚠️ Asegúrate de que el ID coincida con tu XML

        val rolUsuario = MyApp.prefs.tipoUsuario

        if (rolUsuario == 1) {
            // Rol 1: Usuario común. Ocultamos por completo el acceso al formulario
            btnAgregarVoluntariado?.visibility = View.GONE
            Log.d("ROLES", "Acceso denegado (Rol 1): El usuario no puede agregar voluntariados.")
        } else {
            // Rol 2 (Organizador) o Rol 3 (Administrador): Tienen permitido el acceso
            btnAgregarVoluntariado?.visibility = View.VISIBLE
            Log.d("ROLES", "Acceso concedido (Rol $rolUsuario): El usuario puede agregar voluntariados.")

            // Programamos la acción para abrir tu formulario si tiene permisos
            btnAgregarVoluntariado?.setOnClickListener {
                // Aquí colocas tu código de navegación habitual, por ejemplo:
                // parentFragmentManager.beginTransaction().replace(R.id.contenedor, CrearVoluntariadoFragment()).addToBackStack(null).commit()
            }
        }
    }

    /**
     * Se ejecuta de forma segura una vez cargado el mapa de Google
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Centrado de cámara por defecto inicial
        val posicionInicial = LatLng(-33.456789, -70.654321)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionInicial, 11f))

        // Añadimos las utilidades nativas de zoom
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // Traemos de inmediato los pines de eventos guardados en la Base de Datos
        cargarPinesDesdeElBackend()
    }

    private fun verificarPermisoEIniciar() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) {
            iniciarRastreo()
            rastreoActivo = true
        } else {
            pedirPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Comienza a pedirle coordenadas al GPS cada 3 segundos
    private fun iniciarRastreo() {
        val solicitud = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        ).setMinUpdateIntervalMillis(1500L).build()

        try {
            mMap.isMyLocationEnabled = true // Muestra también el punto azul nativo de Google
            fusedLocationClient.requestLocationUpdates(
                solicitud,
                locationCallback,
                requireActivity().mainLooper
            )
        } catch (e: SecurityException) {
            Log.e("GPS", "Error de seguridad por falta de permisos: ${e.message}")
        }
    }

    // Apaga el rastreador de GPS para cuidar el consumo de batería
    private fun detenerRastreo() {
        try {
            mMap.isMyLocationEnabled = false
        } catch (e: SecurityException) { e.printStackTrace() }

        fusedLocationClient.removeLocationUpdates(locationCallback)
        marcadorUsuario?.remove()
        marcadorUsuario = null
    }

    // Salvaguarda: si cambias de pantalla, el GPS se desconecta automáticamente
    override fun onPause() {
        super.onPause()
        if (rastreoActivo) {
            detenerRastreo()
            rastreoActivo = false
        }
    }

    /**
     * Descarga la lista de eventos mediante Retrofit en segundo plano
     * y los renderiza de forma segura en el Hilo Principal.
     */
    private fun cargarPinesDesdeElBackend() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Realizamos el consumo de la API REST
                val listaEventos = RetrofitClient.api.obtenerEventos()
                Log.d("MAPA_RETROFIT", "Eventos descargados con éxito: ${listaEventos.size}")

                // Forzamos la inyección visual en el Main Thread para evitar bloqueos
                withContext(Dispatchers.Main) {
                    for (evento in listaEventos) {
                        val lat = evento.latitud.toDoubleOrNull()
                        val lng = evento.longitud.toDoubleOrNull()

                        if (lat != null && lng != null) {
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(lat, lng))
                                    .title(evento.titulo)
                                    .snippet(evento.descripcion)
                            )
                        } else {
                            Log.w("MAPA_RETROFIT", "Coordenadas inválidas omitidas para el punto: ${evento.titulo}")
                        }
                    }
                    Log.d("MAPA_RETROFIT", "¡Éxito total! Todos los marcadores válidos se han renderizado.")
                }

            } catch (e: Exception) {
                Log.e("MAPA_RETROFIT_ERROR", "Error crítico en el mapeo de pines: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}