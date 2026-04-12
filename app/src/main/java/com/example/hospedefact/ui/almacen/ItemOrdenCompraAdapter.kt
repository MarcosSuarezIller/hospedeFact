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
import com.example.hospedefact.data.models.ItemOrdenCompra

/**
 * Adaptador para items de orden de compra
 */
class ItemOrdenCompraAdapter(
    private val onEliminar: (ItemOrdenCompra) -> Unit
) : ListAdapter<ItemOrdenCompra, ItemOrdenCompraAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_item_orden_nombre)
        private val cantidad: TextView = itemView.findViewById(R.id.text_item_orden_cantidad)
        private val precio: TextView = itemView.findViewById(R.id.text_item_orden_precio)
        private val subtotal: TextView = itemView.findViewById(R.id.text_item_orden_subtotal)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar_item_orden)

        fun bind(item: ItemOrdenCompra) {
            nombre.text = item.productoNombre
            cantidad.text = "${item.cantidad} ${item.unidad}"
            precio.text = "€${String.format("%.2f", item.precioUnitario)}"
            subtotal.text = "€${String.format("%.2f", item.subtotal)}"

            btnEliminar.setOnClickListener {
                onEliminar(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orden_compra_detalle, parent, false)
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