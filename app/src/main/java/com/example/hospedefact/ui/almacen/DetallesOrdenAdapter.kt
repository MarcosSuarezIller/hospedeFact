package com.example.hospedefact.ui.almacen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.ItemOrdenCompra

/**
 * Adaptador para mostrar detalles de items en diálogo
 */
class DetallesOrdenAdapter : ListAdapter<ItemOrdenCompra, DetallesOrdenAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_item_detalles_nombre)
        private val cantidad: TextView = itemView.findViewById(R.id.text_item_detalles_cantidad)
        private val precio: TextView = itemView.findViewById(R.id.text_item_detalles_precio)
        private val subtotal: TextView = itemView.findViewById(R.id.text_item_detalles_subtotal)

        fun bind(item: ItemOrdenCompra) {
            nombre.text = item.productoNombre
            cantidad.text = "${item.cantidad} ${item.unidad}"
            precio.text = "€${String.format("%.2f", item.precioUnitario)}"
            subtotal.text = "€${String.format("%.2f", item.subtotal)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detalles_orden, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemOrdenCompra>() {
        override fun areItemsTheSame(oldItem: ItemOrdenCompra, newItem: ItemOrdenCompra) =
            oldItem.productoId == newItem.productoId

        override fun areContentsTheSame(oldItem: ItemOrdenCompra, newItem: ItemOrdenCompra) =
            oldItem == newItem
    }
}