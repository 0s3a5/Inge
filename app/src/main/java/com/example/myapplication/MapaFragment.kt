package com.example.myapplication

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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 1. Configurar el diseño personalizado del Pop-up (InfoWindow)
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Este método deja el marco del cuadrito por defecto (la flechita abajo)
            override fun getInfoWindow(marker: Marker): View? = null

            // Aquí metemos nuestro diseño XML dentro del marco
            override fun getInfoContents(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.custom_info_window, null)

                val tvTitulo = view.findViewById<TextView>(R.id.tvTituloTienda)
                val tvDireccion = view.findViewById<TextView>(R.id.tvDireccionTienda)
                val tvPrecio = view.findViewById<TextView>(R.id.tvPrecio)

                // Recuperamos la información que le guardamos al pin (el tag)
                val tiendaInfo = marker.tag as? TiendaLocal

                if (tiendaInfo != null) {
                    tvTitulo.text = tiendaInfo.nombre
                    tvDireccion.text = tiendaInfo.direccion
                    tvPrecio.text = tiendaInfo.precioOferta
                }

                return view
            }
        })

        // 2. Crear nuestra lista de prueba local (Mock Data)
        val listaTiendas = listOf(
            TiendaLocal("Centro de Acopio", "Av. Providencia 123", "$1.500", LatLng(-33.440, -70.650)),
            TiendaLocal("Refugio Providencia", "Calle Nueva 456", "$2.300", LatLng(-33.460, -70.630)),
            TiendaLocal("Punto Ñuñoa", "Av. Grecia 789", "$990", LatLng(-33.430, -70.620))
        )

        // 3. Poner los pines en el mapa y guardarles la información
        for (tienda in listaTiendas) {
            val markerOptions = MarkerOptions().position(tienda.ubicacion)
            val marker = mMap.addMarker(markerOptions)

            // ¡MAGIA!: Aquí le adjuntamos toda la info de la tienda al pin
            marker?.tag = tienda
        }

        // 4. Iniciar ubicación real (la función que hicimos en el paso anterior)
        verificarPermisosYUbicar()

        // 5. Borramos el Toast antiguo del clic para que Google Maps haga su trabajo
        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow() // Obligamos a que salga el pop-up
            false // Retornar falso permite que la cámara se centre en el pin al tocarlo
        }
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

    private fun cargarMarcadoresDePrueba() {
        val tienda1 = LatLng(-33.440, -70.650)
        val tienda2 = LatLng(-33.460, -70.630)

        mMap.addMarker(MarkerOptions().position(tienda1).title("Centro de Acopio Centro"))
        mMap.addMarker(MarkerOptions().position(tienda2).title("Refugio Providencia"))
    }
}// Este es el molde para nuestra base de datos local falsa
data class TiendaLocal(
    val nombre: String,
    val direccion: String,
    val precioOferta: String,
    val ubicacion: LatLng
)