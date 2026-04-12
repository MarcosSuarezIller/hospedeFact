package com.example.hospedefact.ui.almacen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Proveedor

/**
 * Adaptador para RecyclerView de Proveedores
 */
class ProveedorAdapter(
    private val onContactarClick: (Proveedor) -> Unit,
    private val onEliminarClick: (Proveedor) -> Unit
) : ListAdapter<Proveedor, ProveedorAdapter.ProveedorViewHolder>(DiffCallback()) {

    inner class ProveedorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_proveedor_nombre)
        private val contacto: TextView = itemView.findViewById(R.id.text_contacto)
        private val email: TextView = itemView.findViewById(R.id.text_email_proveedor)
        private val telefono: TextView = itemView.findViewById(R.id.text_telefono)
        private val tiempoEntrega: TextView = itemView.findViewById(R.id.text_tiempo_entrega)
        private val btnContactar: Button = itemView.findViewById(R.id.btn_contactar)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar_proveedor)

        fun bind(proveedor: Proveedor) {
            nombre.text = "🏢 ${proveedor.nombre}"
            contacto.text = "Contacto: ${proveedor.contacto}"
            email.text = "📧 ${proveedor.email}"
            telefono.text = "📱 ${proveedor.telefono}"
            tiempoEntrega.text = "⏱️ Entrega: ${proveedor.tiempoEntrega} días"

            btnContactar.setOnClickListener {
                onContactarClick(proveedor)
            }

            btnEliminar.setOnClickListener {
                onEliminarClick(proveedor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proveedor, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Proveedor>() {
        override fun areItemsTheSame(oldItem: Proveedor, newItem: Proveedor) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Proveedor, newItem: Proveedor) =
            oldItem == newItem
    }
}