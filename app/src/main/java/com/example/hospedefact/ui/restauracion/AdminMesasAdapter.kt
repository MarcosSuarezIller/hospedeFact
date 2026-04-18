package com.example.hospedefact.ui.restauracion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Mesa

class AdminMesasAdapter(
    private val onEliminar: (Mesa) -> Unit
) : ListAdapter<Mesa, AdminMesasAdapter.MesaViewHolder>(DiffCallback()) {

    inner class MesaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numero: TextView = itemView.findViewById(R.id.text_admin_numero_mesa)
        private val capacidad: TextView = itemView.findViewById(R.id.text_admin_capacidad)
        private val ubicacion: TextView = itemView.findViewById(R.id.text_admin_ubicacion)
        private val estado: TextView = itemView.findViewById(R.id.text_admin_estado)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar_mesa_admin)

        fun bind(mesa: Mesa) {
            numero.text = "Mesa ${mesa.numero}"
            capacidad.text = "Capacidad: ${mesa.capacidad} personas"
            ubicacion.text = "Ubicacion: ${mesa.ubicacion}"

            val (textoEstado, color) = when (mesa.estado) {
                "disponible" -> "Disponible" to android.graphics.Color.GREEN
                "ocupada" -> "Ocupada" to android.graphics.Color.RED
                "reservada" -> "Reservada" to android.graphics.Color.rgb(255, 152, 0)
                else -> "Desconocido" to android.graphics.Color.GRAY
            }

            estado.text = textoEstado
            estado.setTextColor(color)

            btnEliminar.setOnClickListener {
                onEliminar(mesa)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_mesa, parent, false)
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