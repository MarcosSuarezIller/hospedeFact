package com.example.hospedefact.ui.pedidos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.ItemPedido

/**
 * Adaptador para RecyclerView del Carrito
 * Muestra items agregados al carrito
 */
class CarritoAdapter(
    private val onCantidadChange: (ItemPedido, Int) -> Unit,
    private val onEliminar: (ItemPedido) -> Unit
) : ListAdapter<ItemPedido, CarritoAdapter.CarritoViewHolder>(DiffCallback()) {

    /**
     * ViewHolder para cada item del carrito
     */
    inner class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_nombre_carrito)
        private val cantidad: TextView = itemView.findViewById(R.id.text_cantidad)
        private val precio: TextView = itemView.findViewById(R.id.text_precio_carrito)
        private val subtotal: TextView = itemView.findViewById(R.id.text_subtotal)
        private val btnMenos: Button = itemView.findViewById(R.id.btn_menos)
        private val btnMas: Button = itemView.findViewById(R.id.btn_mas)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar)

        fun bind(item: ItemPedido) {
            nombre.text = item.nombre
            cantidad.text = item.cantidad.toString()
            precio.text = "€${String.format("%.2f", item.precioUnitario)}"
            subtotal.text = "€${String.format("%.2f", item.cantidad * item.precioUnitario)}"

            btnMenos.setOnClickListener {
                if (item.cantidad > 1) {
                    val nuevaCantidad = item.cantidad - 1
                    val itemActualizado = item.copy(cantidad = nuevaCantidad)
                    onCantidadChange(itemActualizado, nuevaCantidad)
                }
            }

            btnMas.setOnClickListener {
                val nuevaCantidad = item.cantidad + 1
                val itemActualizado = item.copy(cantidad = nuevaCantidad)
                onCantidadChange(itemActualizado, nuevaCantidad)
            }

            btnEliminar.setOnClickListener {
                onEliminar(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffCallback para optimizar actualizaciones
     */
    class DiffCallback : DiffUtil.ItemCallback<ItemPedido>() {
        override fun areItemsTheSame(oldItem: ItemPedido, newItem: ItemPedido) =
            oldItem.itemId == newItem.itemId

        override fun areContentsTheSame(oldItem: ItemPedido, newItem: ItemPedido) =
            oldItem == newItem
    }
}