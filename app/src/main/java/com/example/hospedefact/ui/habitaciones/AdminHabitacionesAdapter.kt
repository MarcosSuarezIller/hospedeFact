package com.example.hospedefact.ui.habitaciones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Habitacion

class AdminHabitacionesAdapter(
    private val onEliminar: (Habitacion) -> Unit
) : ListAdapter<Habitacion, AdminHabitacionesAdapter.HabitacionViewHolder>(DiffCallback()) {

    inner class HabitacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numero: TextView = itemView.findViewById(R.id.text_admin_numero_habitacion)
        private val tipo: TextView = itemView.findViewById(R.id.text_admin_tipo_habitacion)
        private val precio: TextView = itemView.findViewById(R.id.text_admin_precio_habitacion)
        private val capacidad: TextView = itemView.findViewById(R.id.text_admin_capacidad_habitacion)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar_habitacion_admin)

        fun bind(habitacion: Habitacion) {
            numero.text = "Habitacion ${habitacion.numero}"
            tipo.text = "Tipo: ${habitacion.tipo}"
            precio.text = "Precio noche: €${String.format("%.2f", habitacion.precioNoche)}"
            capacidad.text = "Capacidad: ${habitacion.capacidad} personas"

            btnEliminar.setOnClickListener {
                onEliminar(habitacion)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_habitacion, parent, false)
        return HabitacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Habitacion>() {
        override fun areItemsTheSame(oldItem: Habitacion, newItem: Habitacion) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Habitacion, newItem: Habitacion) =
            oldItem == newItem
    }
}