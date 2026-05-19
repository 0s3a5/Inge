package com.example.myapplication

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        // 1. Vincular las vistas del XML con Kotlin
        val searchView = findViewById<SearchView>(R.id.searchViewMapa)
        val mapContainer = findViewById<View>(R.id.map)
        val rvCatastrofes = findViewById<RecyclerView>(R.id.rvCatastrofes)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // 2. Inicializar el mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 3. Configurar la Lista de Catástrofes (RecyclerView)
        rvCatastrofes.layoutManager = LinearLayoutManager(this)
        rvCatastrofes.adapter = CatastrofeAdapter(DatosLocales.listaCatastrofes)

        // 4. Lógica de la Barra Inferior (Mostrar/Ocultar cosas)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mapa -> {
                    mapContainer.visibility = View.VISIBLE
                    rvCatastrofes.visibility = View.GONE
                    searchView.visibility = View.VISIBLE
                    true
                }
                R.id.nav_catastrofes -> {
                    mapContainer.visibility = View.GONE
                    rvCatastrofes.visibility = View.VISIBLE
                    searchView.visibility = View.GONE // Ocultamos el buscador para que la lista use todo el espacio
                    true
                }
                R.id.nav_voluntariados -> {
                    Toast.makeText(this, "Próximamente: Mis Voluntariados", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_perfil -> {
                    Toast.makeText(this, "Próximamente: Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // 5. Configurar el buscador del mapa
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { locacion ->
                    try {
                        val direcciones = Geocoder(this@MainActivity).getFromLocationName(locacion, 1)
                        if (!direcciones.isNullOrEmpty()) {
                            val coords = LatLng(direcciones[0].latitude, direcciones[0].longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 12f))
                        } else {
                            Toast.makeText(this@MainActivity, "No encontrado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        Toast.makeText(this@MainActivity, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }
            override fun onQueryTextChange(newText: String?) = false
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Centrar en Valparaíso
        val valparaiso = LatLng(-33.0472, -71.6127)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(valparaiso, 10f))

        // Cargar los pines de voluntariados
        for (voluntariado in DatosLocales.listaVoluntariados) {
            val posicion = LatLng(voluntariado.latitud, voluntariado.longitud)
            mMap.addMarker(MarkerOptions().position(posicion).title(voluntariado.titulo).snippet(voluntariado.comuna))
        }
    }
}