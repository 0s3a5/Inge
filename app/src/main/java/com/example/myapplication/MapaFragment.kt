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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                obtenerUbicacionReal()
            } else {
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnGPS = view.findViewById<FloatingActionButton>(R.id.btnGPS)
        val btnFiltro = view.findViewById<View>(R.id.btnFiltro)

        btnGPS.setOnClickListener {
            verificarPermisosYUbicar()
        }

        btnFiltro.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente: Filtros", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        cargarMarcadoresDePrueba()
        verificarPermisosYUbicar()

        mMap.setOnMarkerClickListener { marker ->
            val eventoInfo = marker.tag as? EventoLocal

            if (eventoInfo != null) {
                mostrarDialogoModal(eventoInfo)
            }
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
                Toast.makeText(requireContext(), "No se pudo encontrar tu ubicación física.", Toast.LENGTH_SHORT).show()
                moverCamaraASantiago()
            }
        }
    }

    private fun moverCamaraASantiago() {
        if (!::mMap.isInitialized) return

        val santiagoPredeterminado = LatLng(-33.4402, -70.6534)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(santiagoPredeterminado, 12f))
        Toast.makeText(requireContext(), "Mostrando ubicación predeterminada (Santiago).", Toast.LENGTH_SHORT).show()
    }

    private fun cargarMarcadoresDePrueba() {
        // Base de datos simulada con los nuevos campos agregados
        val listaEventos = listOf(
            EventoLocal(
                titulo = "Bingo",
                descripcion = "Bingo a beneficio de Vicente Muhr",
                tipo_evento = "Personal",
                direccion = "Ejercito 278",
                latitud = -33.45022843,
                longitud = -70.66074543,
                fecha_evento = "2026-05-25 18:00",
                creado_por = "Vicente Muhr",
                verificado = false // Personal, no está verificado
            ),
            EventoLocal(
                titulo = "Banco de sangre",
                descripcion = "Ven a donar al Hospital Barros Luco :)",
                tipo_evento = "Estatal",
                direccion = "Ejercito 441",
                latitud = -33.45244946,
                longitud = -70.66051520,
                fecha_evento = "2026-05-26 09:00",
                creado_por = "MINSAL (Ministerio de Salud)",
                verificado = true // Gubernamental, por ende Verificado
            ),
            EventoLocal(
                titulo = "Albergue HC",
                descripcion = "Albergue ofrecido por el Hogar de Cristo",
                tipo_evento = "ONG",
                direccion = "Vergara 240",
                latitud = -33.45024776,
                longitud = -70.66160922,
                fecha_evento = "2026-05-24 20:00",
                creado_por = "Hogar de Cristo Oficial",
                verificado = true // Institución registrada, Verificado
            )
        )

        for (evento in listaEventos) {
            val colorPin = when (evento.tipo_evento.lowercase()) {
                "estatal" -> BitmapDescriptorFactory.HUE_RED
                "ong" -> BitmapDescriptorFactory.HUE_BLUE
                "personal" -> BitmapDescriptorFactory.HUE_GREEN
                else -> BitmapDescriptorFactory.HUE_ORANGE
            }

            val markerOptions = MarkerOptions()
                .position(LatLng(evento.latitud, evento.longitud))
                .title(evento.titulo)
                .icon(BitmapDescriptorFactory.defaultMarker(colorPin))

            val marker = mMap.addMarker(markerOptions)
            marker?.tag = evento
        }
    }

    private fun mostrarDialogoModal(evento: EventoLocal) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_evento_modal)
        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Vincular las vistas del XML actualizadas
        val tvTitulo = dialog.findViewById<TextView>(R.id.tvModalTitulo)
        val tvVerificado = dialog.findViewById<TextView>(R.id.tvModalVerificado)
        val tvCreador = dialog.findViewById<TextView>(R.id.tvModalCreador)
        val tvTipo = dialog.findViewById<TextView>(R.id.tvModalTipo)
        val tvFecha = dialog.findViewById<TextView>(R.id.tvModalFecha)
        val tvDireccion = dialog.findViewById<TextView>(R.id.tvModalDireccion)
        val tvDescripcion = dialog.findViewById<TextView>(R.id.tvModalDescripcion)
        val btnCerrar = dialog.findViewById<android.widget.Button>(R.id.btnModalCerrar)

        // Asignar los textos dinámicamente
        tvTitulo.text = evento.titulo
        tvCreador.text = "Organizado por: ${evento.creado_por}"
        tvTipo.text = "Tipo: ${evento.tipo_evento}"
        tvFecha.text = "📅 Fecha: ${evento.fecha_evento}"
        tvDireccion.text = "📍 Dirección: ${evento.direccion}"
        tvDescripcion.text = evento.descripcion

        // LÓGICA DE VERIFICACIÓN (Muestra u oculta el ticket según el booleano)
        if (evento.verificado) {
            tvVerificado.visibility = View.VISIBLE
        } else {
            tvVerificado.visibility = View.GONE // Desaparece por completo el espacio del ticket
        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

// Estructura de datos alineada al 100% con tu base de datos
data class EventoLocal(
    val punto_id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val descripcion: String,
    val tipo_evento: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val fecha_evento: String, // String correspondiente al VARCHAR/DATETIME legible
    val creado_por: String,   // Creador en formato String (VARCHAR)
    val verificado: Boolean,  // Verificación en formato Booleano (BOOL)
    val fecha_creacion: String = "2026-05-21T21:10:00Z"
)