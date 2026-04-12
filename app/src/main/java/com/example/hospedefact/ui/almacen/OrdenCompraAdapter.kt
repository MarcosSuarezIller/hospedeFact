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
import com.example.hospedefact.data.models.OrdenCompra
import java.text.SimpleDateFormat

/**
 * Adaptador para RecyclerView de Órdenes de Compra
 */
class OrdenCompraAdapter(
    private val onDetallesClick: (OrdenCompra) -> Unit,
    private val onRecibirClick: (OrdenCompra) -> Unit
) : ListAdapter<OrdenCompra, OrdenCompraAdapter.OrdenViewHolder>(DiffCallback()) {

    inner class OrdenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ordenId: TextView = itemView.findViewById(R.id.text_orden_id)
        private val proveedor: TextView = itemView.findViewById(R.id.text_proveedor_orden)
        private val fecha: TextView = itemView.findViewById(R.id.text_fecha_orden)
        private val total: TextView = itemView.findViewById(R.id.text_total_orden)
        private val estado: TextView = itemView.findViewById(R.id.text_estado_orden)
        private val btnDetalles: Button = itemView.findViewById(R.id.btn_detalles_orden)
        private val btnRecibir: Button = itemView.findViewById(R.id.btn_recibir_orden)

        fun bind(orden: OrdenCompra) {
            ordenId.text = "Orden #${orden.id.take(8).uppercase()}"
            proveedor.text = "Proveedor: ${orden.proveedorNombre}"

            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            fecha.text = "Fecha: ${dateFormat.format(orden.fecha)}"

            total.text = "Total: €${String.format("%.2f", orden.total)}"

            // Mostrar estado
            val (textoEstado, color) = when (orden.estado) {
                "pendiente" -> "⏳ Pendiente" to android.graphics.Color.rgb(255, 152, 0)
                "confirmada" -> "✅ Confirmada" to android.graphics.Color.GREEN
                "entregada" -> "✔️ Entregada" to android.graphics.Color.GREEN
                "cancelada" -> "❌ Cancelada" to android.graphics.Color.RED
                else -> "❓ Desconocido" to android.graphics.Color.GRAY
            }

            estado.text = textoEstado
            estado.setTextColor(color)

            // Mostrar botón recibir solo si está confirmada
            btnRecibir.visibility = if (orden.estado == "confirmada") View.VISIBLE else View.GONE

            btnDetalles.setOnClickListener {

                onDetallesClick(orden)
            }

            btnRecibir.setOnClickListener {

                onRecibirClick(orden)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdenViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orden_compra, parent, false)
        return OrdenViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdenViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<OrdenCompra>() {
        override fun areItemsTheSame(oldItem: OrdenCompra, newItem: OrdenCompra) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrdenCompra, newItem: OrdenCompra) =
            oldItem == newItem
    }
}