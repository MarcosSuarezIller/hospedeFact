package com.example.hospedefact.ui.almacen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.ItemOrdenCompra

/**
 * Adaptador para items de recepción de mercancía
 * Permite confirmar cantidades recibidas
 */
class ItemRecepcionAdapter(
    private val onCantidadChange: (ItemOrdenCompra, Int) -> Unit
) : ListAdapter<ItemOrdenCompra, ItemRecepcionAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_item_recepcion_nombre)
        private val cantidadOrdenada: TextView = itemView.findViewById(R.id.text_cantidad_ordenada)
        private val inputCantidadRecibida: EditText = itemView.findViewById(R.id.input_cantidad_recibida)
        private val btnMenos: Button = itemView.findViewById(R.id.btn_menos_recepcion)
        private val btnMas: Button = itemView.findViewById(R.id.btn_mas_recepcion)

        fun bind(item: ItemOrdenCompra) {
            nombre.text = item.productoNombre
            cantidadOrdenada.text = "Ordenado: ${item.cantidad} ${item.unidad}"
            inputCantidadRecibida.setText(item.cantidadRecibida.toString())

            btnMenos.setOnClickListener {
                val actual = inputCantidadRecibida.text.toString().toIntOrNull() ?: 0
                if (actual > 0) {
                    val nuevo = actual - 1
                    inputCantidadRecibida.setText(nuevo.toString())
                    onCantidadChange(item, nuevo)
                }
            }

            btnMas.setOnClickListener {
                val actual = inputCantidadRecibida.text.toString().toIntOrNull() ?: 0
                if (actual < item.cantidad) {
                    val nuevo = actual + 1
                    inputCantidadRecibida.setText(nuevo.toString())
                    onCantidadChange(item, nuevo)
                }
            }

            inputCantidadRecibida.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val valor = inputCantidadRecibida.text.toString().toIntOrNull() ?: item.cantidad
                    val cantidad = minOf(valor, item.cantidad)  // No puede ser mayor a lo ordenado
                    inputCantidadRecibida.setText(cantidad.toString())
                    onCantidadChange(item, cantidad)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recepcion_detalle, parent, false)
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