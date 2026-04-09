package com.example.hospedefact.ui.huespedes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Huesped

/**
 * Adaptador para RecyclerView de Huéspedes
 * Muestra lista de huéspedes en pantalla
 */
class HuespedAdapter(
    private val onItemClick: (Huesped) -> Unit
) : ListAdapter<Huesped, HuespedAdapter.HuespedViewHolder>(DiffCallback()) {

    /**
     * ViewHolder para cada item de la lista
     */
    inner class HuespedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_nombre)
        private val habitacion: TextView = itemView.findViewById(R.id.text_habitacion)
        private val email: TextView = itemView.findViewById(R.id.text_email)

        fun bind(huesped: Huesped) {
            nombre.text = "👤 ${huesped.nombre}"
            habitacion.text = "🛏️ Hab: ${huesped.habitacion}"
            email.text = "📧 ${huesped.email}"

            itemView.setOnClickListener {
                onItemClick(huesped)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HuespedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_huesped, parent, false)
        return HuespedViewHolder(view)
    }

    override fun onBindViewHolder(holder: HuespedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffCallback para optimizar actualizaciones
     */
    class DiffCallback : DiffUtil.ItemCallback<Huesped>() {
        override fun areItemsTheSame(oldItem: Huesped, newItem: Huesped) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Huesped, newItem: Huesped) =
            oldItem == newItem
    }
}
