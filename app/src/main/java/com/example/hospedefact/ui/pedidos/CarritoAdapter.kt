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
 * Adaptador para el RecyclerView que gestiona la visualización del carrito de compras.
 * Permite listar los productos seleccionados para un pedido y ajustar sus cantidades o eliminarlos.
 *
 * @property onCantidadChange Callback que se ejecuta cuando se modifica la cantidad de un artículo.
 * @property onEliminar Callback que se ejecuta cuando se decide quitar un artículo del carrito.
 */
class CarritoAdapter(
    private val onCantidadChange: (ItemPedido, Int) -> Unit,
    private val onEliminar: (ItemPedido) -> Unit
) : ListAdapter<ItemPedido, CarritoAdapter.CarritoViewHolder>(DiffCallback()) {

    /**
     * ViewHolder para cada item del carrito
     */
    /**
     * ViewHolder que gestiona la representación visual de un artículo individual dentro del carrito.
     */
    inner class CarritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_nombre_carrito)
        private val cantidad: TextView = itemView.findViewById(R.id.text_cantidad)
        private val precio: TextView = itemView.findViewById(R.id.text_precio_carrito)
        private val subtotal: TextView = itemView.findViewById(R.id.text_subtotal)
        private val btnMenos: Button = itemView.findViewById(R.id.btn_menos)
        private val btnMas: Button = itemView.findViewById(R.id.btn_mas)
        private val btnEliminar: Button = itemView.findViewById(R.id.btn_eliminar)

        /**
         * Vincula los datos de un [ItemPedido] con las vistas correspondientes.
         * Calcula dinámicamente el subtotal del ítem basándose en la cantidad y el precio unitario.
         * Configura los listeners para los botones de incremento, decremento y eliminación.
         * 
         * @param item El artículo del pedido que se va a mostrar.
         */
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
     * Implementación de [DiffUtil.ItemCallback] para optimizar la actualización de la lista
     * del carrito, comparando los artículos por su identificador único.
     */
    class DiffCallback : DiffUtil.ItemCallback<ItemPedido>() {
        override fun areItemsTheSame(oldItem: ItemPedido, newItem: ItemPedido) =
            oldItem.itemId == newItem.itemId

        override fun areContentsTheSame(oldItem: ItemPedido, newItem: ItemPedido) =
            oldItem == newItem
    }
}