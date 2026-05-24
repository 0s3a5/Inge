package com.example.myapplication

import java.util.UUID
import android.Manifest
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 1. SOLO DECLARAMOS la variable aquí arriba (sin inicializarla aún)
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    // 2. REGISTRAMOS el contrato en el onCreate (El momento exacto y seguro)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                obtenerUbicacionReal()
            } else {
                // EL USUARIO DIJO QUE NO: Mandamos la cámara a Santiago
                Toast.makeText(requireContext(), "Permiso denegado.", Toast.LENGTH_SHORT).show()
                moverCamaraASantiago()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el cliente de GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Cargar el mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar los botones
        val btnGPS = view.findViewById<FloatingActionButton>(R.id.btnGPS)
        val btnFiltro = view.findViewById<View>(R.id.btnFiltro)

        btnGPS.setOnClickListener {
            verificarPermisosYUbicar() // Si tocamos el botón, volvemos a buscar el GPS
        }

        btnFiltro.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente: Filtros", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        cargarMarcadoresDePrueba()
        verificarPermisosYUbicar()

        // MAGIA DEL MODAL: Qué pasa al tocar el pin
        mMap.setOnMarkerClickListener { marker ->
            val eventoInfo = marker.tag as? EventoLocal

            if (eventoInfo != null) {
                mostrarDialogoModal(eventoInfo)
            }

            // Retornar TRUE es CRÍTICO: significa "Yo me encargo del clic, Google Maps no hagas nada más".
            // Esto desactiva el cuadrito blanco por defecto.
            true
        }
    }

    private fun verificarPermisosYUbicar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionReal()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionReal() {
        if (!::mMap.isInitialized) return

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val miUbicacionReal = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacionReal, 15f))
            } else {
                // EL GPS FALLÓ O ESTÁ APAGADO: Mandamos la cámara a Santiago
                Toast.makeText(requireContext(), "No se pudo encontrar tu ubicación física.", Toast.LENGTH_SHORT).show()
                moverCamaraASantiago()
            }
        }
    }

    private fun moverCamaraASantiago() {
        if (!::mMap.isInitialized) return

        // Coordenadas del centro de Santiago (Plaza de Armas / La Moneda)
        val santiagoPredeterminado = LatLng(-33.4402, -70.6534)

        // Movemos la cámara con un zoom nivel 12 (suficiente para ver toda la ciudad)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(santiagoPredeterminado, 12f))

        Toast.makeText(requireContext(), "Mostrando ubicación predeterminada (Santiago).", Toast.LENGTH_SHORT).show()
    }

    private fun cargarMarcadoresDePrueba() {
        // Recreamos la base de datos local basándonos EXACTAMENTE en tu imagen
        val listaEventos = listOf(
            EventoLocal(
                titulo = "Bingo",
                descripcion = "Bingo a benefio de Vicente Muhr",
                tipo_evento = "Personal",
                direccion = "Ejercito 278",
                latitud = -33.45022843,
                longitud = -70.66074543,
                fecha_evento = "2026-05-25 18:00:00+00"
            ),
            EventoLocal(
                titulo = "Banco de sangre",
                descripcion = "ven a donar al Hospital Barros Luco:)",
                tipo_evento = "Estatal",
                direccion = "ejercito 441",
                latitud = -33.45244946,
                longitud = -70.66051520,
                fecha_evento = "2026-05-26 09:00:00+00"
            ),
            EventoLocal(
                titulo = "Albergue HC",
                descripcion = "Albergue ofrecido por el Hogar de Cristo ",
                tipo_evento = "ONG",
                direccion = "Vergara 240",
                latitud = -33.45024776,
                longitud = -70.66160922,
                fecha_evento = "2026-05-24 20:00:00+00"
            )
        )

        // Colocar los pines en el mapa
        for (evento in listaEventos) {
            // LÓGICA DE COLORES
            val colorPin = when (evento.tipo_evento.lowercase()) {
                "estatal" -> BitmapDescriptorFactory.HUE_RED
                "ong" -> BitmapDescriptorFactory.HUE_BLUE
                "personal" -> BitmapDescriptorFactory.HUE_GREEN
                else -> BitmapDescriptorFactory.HUE_ORANGE
            }

            // Creamos el pin asignándole la posición, el título y el COLOR
            val markerOptions = MarkerOptions()
                .position(LatLng(evento.latitud, evento.longitud))
                .title(evento.titulo)
                .icon(BitmapDescriptorFactory.defaultMarker(colorPin))

            val marker = mMap.addMarker(markerOptions)

            // Vinculamos la info para que el Pop-up (Modal) funcione al tocarlo
            marker?.tag = evento
        }
    }

    // Función que crea la ventana que bloquea la app
    private fun mostrarDialogoModal(evento: EventoLocal) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_evento_modal)

        // Bloquea toda la app hasta que presione el botón de cerrar.
        dialog.setCancelable(false)

        // Hacemos que el fondo de la ventana sea transparente para que se vean los bordes
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Vincular los textos del XML
        val tvTitulo = dialog.findViewById<TextView>(R.id.tvModalTitulo)
        val tvTipo = dialog.findViewById<TextView>(R.id.tvModalTipo)
        val tvDireccion = dialog.findViewById<TextView>(R.id.tvModalDireccion)
        val tvDescripcion = dialog.findViewById<TextView>(R.id.tvModalDescripcion)
        val btnCerrar = dialog.findViewById<android.widget.Button>(R.id.btnModalCerrar)

        // Llenar los datos de tu Base de Datos SQL simulada
        tvTitulo.text = evento.titulo
        tvTipo.text = "Tipo: ${evento.tipo_evento}"
        tvDireccion.text = evento.direccion
        tvDescripcion.text = evento.descripcion

        // Lógica del botón para desbloquear la app
        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

// Estructura idéntica a tu tabla SQL
data class EventoLocal(
    val punto_id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val descripcion: String,
    val tipo_evento: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val fecha_evento: String,
    val creado_por: String = UUID.randomUUID().toString(),
    val fecha_creacion: String = "2026-05-21T21:10:00Z"
)