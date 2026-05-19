package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CatastrofeAdapter(private val lista: List<Catastrofe>) : RecyclerView.Adapter<CatastrofeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipoAlerta: TextView = view.findViewById(R.id.tvTipoAlerta)
        val tvTitulo: TextView = view.findViewById(R.id.tvTituloCatastrofe)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacionCatastrofe)
        val tvTiempo: TextView = view.findViewById(R.id.tvTiempo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_catastrofe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val catastrofe = lista[position]
        holder.tvTipoAlerta.text = catastrofe.tipoAlerta
        holder.tvTitulo.text = catastrofe.titulo
        holder.tvUbicacion.text = catastrofe.ubicacion
        holder.tvTiempo.text = catastrofe.tiempo
    }

    override fun getItemCount() = lista.size
}