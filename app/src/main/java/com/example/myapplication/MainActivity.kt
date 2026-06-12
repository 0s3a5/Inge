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
        setContentView(R.layout.activity_main) // 👈 Vincula el XML que acabamos de asegurar

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_mapa -> {
                    cargarFragmento(MapaFragment())
                    true
                }
                R.id.nav_catastrofes -> {
                    // cargarFragmento(CatastrofesFragment()) // Descomenta si ya lo tienes creado
                    true
                }
                R.id.nav_voluntariados -> {
                    Toast.makeText(this, "Próximamente: Mis Voluntariados", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_perfil -> {
                    cargarFragmento(PerfilFragment())
                    true
                }
                else -> false
            }
        }

        // 🚀 Cargamos el mapa solo si es la primera vez que se crea la actividad
        if (savedInstanceState == null) {
            cargarFragmento(MapaFragment())
        }
    }

    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}