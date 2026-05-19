package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VoluntariadoAdapter(private var lista: List<Voluntariado>) :
    RecyclerView.Adapter<VoluntariadoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvComuna: TextView = view.findViewById(R.id.tvComuna)
        val tvRequisitos: TextView = view.findViewById(R.id.tvRequisitos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voluntariado, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val voluntariado = lista[position]
        holder.tvTitulo.text = voluntariado.titulo
        holder.tvComuna.text = voluntariado.comuna
        holder.tvRequisitos.text = "Requisitos: ${voluntariado.requisitos}"
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Voluntariado>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}