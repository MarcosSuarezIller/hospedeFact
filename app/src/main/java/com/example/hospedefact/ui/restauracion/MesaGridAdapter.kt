package com.example.hospedefact.ui.restauracion

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Mesa

/**
 * Adaptador en forma de grid para visualizar mesas
 */
class MesaGridAdapter(
    private val onMesaClick: (Mesa) -> Unit
) : ListAdapter<Mesa, MesaGridAdapter.MesaViewHolder>(DiffCallback()) {

    inner class MesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numero: TextView = itemView.findViewById(R.id.text_numero_mesa)
        private val capacidad: TextView = itemView.findViewById(R.id.text_capacidad_mesa)
        private val estado: TextView = itemView.findViewById(R.id.text_estado_mesa_grid)
        private val container: LinearLayout = itemView.findViewById(R.id.container_mesa)

        fun bind(mesa: Mesa) {
            numero.text = "Mesa ${mesa.numero}"
            capacidad.text = "Cap: ${mesa.capacidad}"

            val (textoEstado, color) = when (mesa.estado) {
                "disponible" -> "Disponible" to Color.GREEN
                "ocupada" -> "Ocupada" to Color.RED
                "reservada" -> "Reservada" to Color.rgb(255, 152, 0)
                "mantenimiento" -> "Mantenimiento" to Color.GRAY
                else -> "Desconocido" to Color.GRAY
            }

            estado.text = textoEstado
            estado.setTextColor(color)
            container.setBackgroundColor(color.let { it and 0x33FFFFFF or 0x55000000.toInt() })

            container.setOnClickListener {
                onMesaClick(mesa)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mesa_grid, parent, false)
        return MesaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Mesa>() {
        override fun areItemsTheSame(oldItem: Mesa, newItem: Mesa) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Mesa, newItem: Mesa) =
            oldItem == newItem
    }
}