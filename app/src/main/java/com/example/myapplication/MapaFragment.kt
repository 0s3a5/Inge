package com.example.myapplication
import java.util.UUID
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
    // Esta variable es la que lee el GPS del teléfono
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Este es el "lanzador" que muestra la ventanita preguntando por permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            obtenerUbicacionReal()
        } else {
            Toast.makeText(requireContext(), "Permiso denegado. No podemos mostrar tu ubicación.", Toast.LENGTH_LONG).show()
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

    // Agrega este import arriba en tu archivo si no lo tienes:
    // import android.app.Dialog
    // import android.graphics.Color
    // import android.graphics.drawable.ColorDrawable

    private fun cargarMarcadoresDePrueba() {
        // 1. Creamos tu evento ficticio exacto en Ejército 441
        val eventoEjercito = EventoLocal(
            titulo = "Punto de Encuentro Universitario",
            descripcion = "Acopio de materiales de emergencia y reunión de voluntarios. Se necesita agua y linternas.",
            tipo_evento = "pop-up",
            direccion = "Av. Ejército Libertador 441, Santiago",
            latitud = -33.451800,
            longitud = -70.660600,
            fecha_evento = "2026-05-25 10:00:00+00"
        )

        // Lo agregamos al mapa
        val markerOptions = MarkerOptions().position(LatLng(eventoEjercito.latitud, eventoEjercito.longitud)).title(eventoEjercito.titulo)
        val marker = mMap.addMarker(markerOptions)

        // Adjuntamos el objeto completo al pin
        marker?.tag = eventoEjercito
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        cargarMarcadoresDePrueba()
        verificarPermisosYUbicar()

        // 2. MAGIA DEL MODAL: Qué pasa al tocar el pin
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

    // 3. Función que crea la ventana que bloquea la app
    private fun mostrarDialogoModal(evento: EventoLocal) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_evento_modal)

        // ¡ESTA LÍNEA ES LA MÁS IMPORTANTE!
        // Impide que el usuario cierre el pop-up tocando fuera de él.
        // Bloquea toda la app hasta que presione el botón de cerrar.
        dialog.setCancelable(false)

        // Hacemos que el fondo de la ventana sea transparente para que se vean los bordes
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        // Hacemos que el diálogo ocupe casi todo el ancho de la pantalla
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
            dialog.dismiss() // Cierra la ventana
        }

        // Mostrar la ventana en pantalla
        dialog.show()
    }

    private fun verificarPermisosYUbicar() {
        // ¿Ya tenemos permiso?
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionReal()
        } else {
            // Si no tenemos permiso, lo pedimos
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission") // Anotación porque ya verificamos los permisos arriba
    private fun obtenerUbicacionReal() {
        if (!::mMap.isInitialized) return

        // Esto activa el famoso "Punto Azul" en el mapa de Google
        mMap.isMyLocationEnabled = true
        // Desactivamos el botón de GPS por defecto de Google para usar el nuestro
        mMap.uiSettings.isMyLocationButtonEnabled = false

        // Leemos la última ubicación conocida del teléfono
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // ¡Éxito! Tenemos latitud y longitud real
                val miUbicacionReal = LatLng(location.latitude, location.longitude)

                // Animamos la cámara hacia esa ubicación con un buen zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacionReal, 15f))
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener la ubicación. Comprueba que tu GPS esté encendido.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}


// Estructura idéntica a tu tabla SQL
data class EventoLocal(
    val punto_id: String = UUID.randomUUID().toString(), // Genera un UUID automático
    val titulo: String,
    val descripcion: String,
    val tipo_evento: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val fecha_evento: String,
    val creado_por: String = UUID.randomUUID().toString(),
    val fecha_creacion: String = "2026-05-21T21:10:00Z" // Formato TIMESTAMP
)