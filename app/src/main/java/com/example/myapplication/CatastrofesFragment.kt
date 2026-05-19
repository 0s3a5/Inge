package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CatastrofesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_catastrofes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvCatastrofes = view.findViewById<RecyclerView>(R.id.rvCatastrofes)

        // Configurar la lista con nuestro adaptador y los datos locales
        rvCatastrofes.layoutManager = LinearLayoutManager(requireContext())
        rvCatastrofes.adapter = CatastrofeAdapter(DatosLocales.listaCatastrofes)
    }
}