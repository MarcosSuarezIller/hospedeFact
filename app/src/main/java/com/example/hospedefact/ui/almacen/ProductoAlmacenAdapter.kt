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
import com.example.hospedefact.data.models.ProductoAlmacen
import com.example.hospedefact.data.models.obtenerEstadoStock
import com.example.hospedefact.data.models.EstadoStock

/**
 * Adaptador para RecyclerView de Productos en Almacén
 */
class ProductoAlmacenAdapter(
    private val onEditarClick: (ProductoAlmacen) -> Unit,
    private val onMovimientoClick: (ProductoAlmacen) -> Unit
) : ListAdapter<ProductoAlmacen, ProductoAlmacenAdapter.ProductoViewHolder>(DiffCallback()) {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_producto_nombre)
        private val stock: TextView = itemView.findViewById(R.id.text_stock)
        private val estado: TextView = itemView.findViewById(R.id.text_estado_stock)
        private val precio: TextView = itemView.findViewById(R.id.text_precio_producto)
        private val ubicacion: TextView = itemView.findViewById(R.id.text_ubicacion)
        private val btnEditar: Button = itemView.findViewById(R.id.btn_editar_producto)
        private val btnMovimiento: Button = itemView.findViewById(R.id.btn_movimiento_stock)

        fun bind(producto: ProductoAlmacen) {
            nombre.text = "📦 ${producto.nombre}"
            stock.text = "Stock: ${producto.stockActual} ${producto.unidad}"
            precio.text = "€${String.format("%.2f", producto.precioVenta)}"
            ubicacion.text = "📍 ${producto.ubicacion}"

            // Mostrar estado del stock con colores
            val estadoStock = producto.obtenerEstadoStock()
            val (textoEstado, color) = when (estadoStock) {
                EstadoStock.NORMAL -> "✅ Normal" to android.graphics.Color.GREEN
                EstadoStock.BAJO -> "⚠️ Bajo" to android.graphics.Color.rgb(255, 152, 0) // Naranja
                EstadoStock.CRITICO -> "🔴 Crítico" to android.graphics.Color.RED
                EstadoStock.AGOTADO -> "❌ Agotado" to android.graphics.Color.RED
                EstadoStock.EXCESO -> "⬆️ Exceso" to android.graphics.Color.BLUE
            }

            estado.text = textoEstado
            estado.setTextColor(color)

            btnEditar.setOnClickListener {
                onEditarClick(producto)
            }

            btnMovimiento.setOnClickListener {
                onMovimientoClick(producto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_almacen, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ProductoAlmacen>() {
        override fun areItemsTheSame(oldItem: ProductoAlmacen, newItem: ProductoAlmacen) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProductoAlmacen, newItem: ProductoAlmacen) =
            oldItem == newItem
    }
}