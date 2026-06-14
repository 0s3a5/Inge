package com.example.myapplication // ⚠️ REVISA: Asegúrate de que coincida con tu paquete real

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View // 🚀 CRÍTICO: Resuelve el error de "visibility"
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView // 🚀 CRÍTICO: Resuelve el error de "ImageView"
import android.widget.TextView
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
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var marcadorUsuario: Marker? = null
    private var rastreoActivo = false

    // 🗂️ Variables para el motor de filtrado en tiempo real
    private var listaOriginalEventos: List<EventoResponseData> = emptyList()
    private var categoriaSeleccionada: String = "Todos"
    private var textoBusqueda: String = ""

    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            iniciarRastreo()
            rastreoActivo = true
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(resultado: LocationResult) {
                val ubicacion = resultado.lastLocation ?: return
                val posicion = LatLng(ubicacion.latitude, ubicacion.longitude)

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

                if (rastreoActivo) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16f))
                }
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // 🔍 Buscador de texto
        val etBuscadorMapa = view.findViewById<EditText>(R.id.etBuscadorMapa)
        etBuscadorMapa?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textoBusqueda = s.toString().trim().lowercase()
                aplicarFiltrosCombinados()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 🏷️ Filtro de Chips
        val chipGroupCategorias = view.findViewById<ChipGroup>(R.id.chipGroupCategorias)
        chipGroupCategorias?.setOnCheckedStateChangeListener { _, checkedIds ->
            categoriaSeleccionada = when (checkedIds.firstOrNull()) {
                R.id.chipOng -> "ONG"
                R.id.chipEstatal -> "Estatal"
                R.id.chipPersonal -> "Personal"
                else -> "Todos"
            }
            aplicarFiltrosCombinados()
        }

        // Botón Ubicación Manual
        view.findViewById<FloatingActionButton>(R.id.btnUbicacionManual)?.setOnClickListener {
            val miZonaPruebas = LatLng(-33.456789, -70.654321)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miZonaPruebas, 14f))
        }

        // Botón GPS
        view.findViewById<FloatingActionButton>(R.id.btnGPS)?.setOnClickListener {
            if (rastreoActivo) {
                detenerRastreo()
                rastreoActivo = false
            } else {
                verificarPermisoEIniciar()
            }
        }

        // 🔐 Control de acceso por Roles
        val btnAbrirCrearEvento = view.findViewById<FloatingActionButton>(R.id.btnAgregarVoluntariado)
        val rolUsuario = MyApp.prefs.tipoUsuario

        if (rolUsuario == 1) {
            btnAbrirCrearEvento?.visibility = View.GONE
        } else {
            btnAbrirCrearEvento?.visibility = View.VISIBLE
            btnAbrirCrearEvento?.setOnClickListener {
                if (MyApp.prefs.userId == null) {
                    Toast.makeText(requireContext(), "Debes iniciar sesión para crear eventos.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (MyApp.prefs.tipoUsuario == 2) {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, CrearEventoFragment())
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "Acceso Denegado: Solo Organizadores.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val posicionInicial = LatLng(-33.456789, -70.654321)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicionInicial, 11f))

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // 🪄 CONFIGURACIÓN DEL CUADRO FLOTANTE NATIVO CON TU DISEÑO FIJADO
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // Usa el recuadro blanco estándar de Google Maps
            }

            override fun getInfoContents(marker: Marker): View? {
                if (marker == marcadorUsuario || marker.title == "Tú estás aquí") return null

                val evento = marker.tag as? EventoResponseData ?: return null

                // Inflamos tu diseño modificado con el ancho fijo
                val vistaCuadro = layoutInflater.inflate(R.layout.modal_mapa_voluntariado, null)

                val tvTitulo = vistaCuadro.findViewById<TextView>(R.id.tvModalTitulo)
                val ivVerificado = vistaCuadro.findViewById<ImageView>(R.id.ivModalVerificado) // ✅ Sin error
                val tvCreador = vistaCuadro.findViewById<TextView>(R.id.tvModalCreador)
                val tvTipo = vistaCuadro.findViewById<TextView>(R.id.tvModalTipo)
                val tvFecha = vistaCuadro.findViewById<TextView>(R.id.tvModalFecha)
                val tvDireccion = vistaCuadro.findViewById<TextView>(R.id.tvModalDireccion)
                val tvDescripcion = vistaCuadro.findViewById<TextView>(R.id.tvModalDescripcion)
                val btnCerrar = vistaCuadro.findViewById<Button>(R.id.btnModalCerrar)

                // Inyectamos los datos de la BD en tus elementos
                tvTitulo?.text = evento.titulo
                tvCreador?.text = "Organizado por: ${evento.nombre_creador}"
                tvTipo?.text = "Tipo: ${evento.tipo_evento.uppercase()}"
                tvDireccion?.text = "📍 Dirección: ${evento.direccion}"
                tvDescripcion?.text = evento.descripcion ?: "Sin descripción detallada."

                // Limpieza de formato de fecha ISO
                val fechaOriginal = evento.fecha_evento
                val fechaLimpia = if (fechaOriginal.contains("T")) {
                    fechaOriginal.split("T")[0]
                } else {
                    fechaOriginal
                }
                tvFecha?.text = "📅 Fecha: $fechaLimpia"

                // 🎫 Se activa la visibilidad del icono del ticket de forma segura
                ivVerificado?.visibility = View.VISIBLE // ✅ Sin error gracias al import View

                // Adaptamos el botón estéticamente
                btnCerrar?.text = "TOCA EL MAPA PARA CERRAR"

                return vistaCuadro
            }
        })

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

    private fun iniciarRastreo() {
        val solicitud = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1500L).build()
        try {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.requestLocationUpdates(solicitud, locationCallback, requireActivity().mainLooper)
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    private fun detenerRastreo() {
        try { mMap.isMyLocationEnabled = false } catch (e: SecurityException) { e.printStackTrace() }
        fusedLocationClient.removeLocationUpdates(locationCallback)
        marcadorUsuario?.remove()
        marcadorUsuario = null
    }

    override fun onPause() {
        super.onPause()
        if (rastreoActivo) {
            detenerRastreo()
            rastreoActivo = false
        }
    }

    private fun cargarPinesDesdeElBackend() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                listaOriginalEventos = RetrofitClient.api.obtenerEventos()
                Log.d("MAPA_RETROFIT", "📡 ¡Conexión exitosa! Llegaron ${listaOriginalEventos.size} eventos.")

                if (listaOriginalEventos.isEmpty()) {
                    Toast.makeText(requireContext(), "Base de datos vacía.", Toast.LENGTH_LONG).show()
                } else {
                    actualizarMarcadoresEnMapa(listaOriginalEventos)
                }
            } catch (e: Exception) {
                Log.e("MAPA_RETROFIT_ERROR", "❌ Error crítico: ${e.message}")
                Toast.makeText(requireContext(), "Fallo de conexión", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun aplicarFiltrosCombinados() {
        if (!::mMap.isInitialized) return
        val listaFiltrada = listaOriginalEventos.filter { evento ->
            val coincideCategoria = if (categoriaSeleccionada == "Todos") true
            else evento.tipo_evento.equals(categoriaSeleccionada, ignoreCase = true)

            val coincideTexto = if (textoBusqueda.isEmpty()) true
            else evento.titulo.lowercase().contains(textoBusqueda) || evento.direccion.lowercase().contains(textoBusqueda)

            coincideCategoria && coincideTexto
        }
        actualizarMarcadoresEnMapa(listaFiltrada)
    }

    private fun actualizarMarcadoresEnMapa(eventos: List<EventoResponseData>) {
        val posicionUsuarioAnterior = marcadorUsuario?.position
        mMap.clear()

        // Mantiene tu marcador azul de "Tú estás aquí" si el GPS está activo
        if (posicionUsuarioAnterior != null) {
            marcadorUsuario = mMap.addMarker(
                MarkerOptions().position(posicionUsuarioAnterior).title("Tú estás aquí")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }

        // Dibujar los pines de la Base de Datos con sus colores correspondientes
        for (evento in eventos) {
            val lat = evento.latitud.toDoubleOrNull()
            val lng = evento.longitud.toDoubleOrNull()

            if (lat != null && lng != null) {

                // 🎨 ASIGNACIÓN DE COLORES SEGÚN EL TIPO
                val colorPin = when (evento.tipo_evento.lowercase().trim()) {
                    "ong" -> BitmapDescriptorFactory.HUE_BLUE       // 🔵 ONG = Azul
                    "personal" -> BitmapDescriptorFactory.HUE_GREEN  // 🟢 Personal = Verde
                    "estatal" -> BitmapDescriptorFactory.HUE_RED     // 🔴 Estatal = Rojo
                    else -> BitmapDescriptorFactory.HUE_ORANGE       // 🟠 Por si acaso hay otro tipo
                }

                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lng))
                        .title(evento.titulo)
                        .icon(BitmapDescriptorFactory.defaultMarker(colorPin)) // 🚀 Aplica el color aquí
                )
                marker?.tag = evento // Amarra el objeto al pin para el cuadro de info
            }
        }
    }
}