package com.example.myapplication

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapaFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchView = view.findViewById<SearchView>(R.id.searchViewMapa)

        // En fragmentos, usamos childFragmentManager para buscar otro fragmento anidado (el mapa)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { locacion ->
                    try {
                        val direcciones = Geocoder(requireContext()).getFromLocationName(locacion, 1)
                        if (!direcciones.isNullOrEmpty()) {
                            val coords = LatLng(direcciones[0].latitude, direcciones[0].longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 12f))
                        } else {
                            Toast.makeText(requireContext(), "No encontrado", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }
            override fun onQueryTextChange(newText: String?) = false
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val valparaiso = LatLng(-33.0472, -71.6127)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(valparaiso, 10f))

        for (voluntariado in DatosLocales.listaVoluntariados) {
            val posicion = LatLng(voluntariado.latitud, voluntariado.longitud)
            mMap.addMarker(MarkerOptions().position(posicion).title(voluntariado.titulo).snippet(voluntariado.comuna))
        }
    }
}