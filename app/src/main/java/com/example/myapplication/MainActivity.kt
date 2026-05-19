package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Cargar el mapa por defecto al abrir la app
        if (savedInstanceState == null) {
            cargarFragmento(MapaFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mapa -> {
                    cargarFragmento(MapaFragment())
                    true
                }
                R.id.nav_catastrofes -> {
                    cargarFragmento(CatastrofesFragment())
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
    }

    // Función auxiliar para reemplazar fragmentos en el contenedor
    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}