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
import com.example.hospedefact.data.models.MenuItem

/**
 * Adaptador para RecyclerView del Menú
 * Muestra items disponibles para agregar al carrito
 */
class MenuAdapter(
    private val onAgregarClick: (MenuItem) -> Unit
) : ListAdapter<MenuItem, MenuAdapter.MenuViewHolder>(DiffCallback()) {

    /**
     * ViewHolder para cada item del menú
     */
    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.text_nombre_item)
        private val descripcion: TextView = itemView.findViewById(R.id.text_descripcion)
        private val precio: TextView = itemView.findViewById(R.id.text_precio)
        private val btnAgregar: Button = itemView.findViewById(R.id.btn_agregar_carrito)

        fun bind(item: MenuItem) {
            nombre.text = item.nombre
            descripcion.text = item.descripcion
            precio.text = "€${String.format("%.2f", item.precio)}"

            btnAgregar.setOnClickListener {
                onAgregarClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffCallback para optimizar actualizaciones
     */
    class DiffCallback : DiffUtil.ItemCallback<MenuItem>() {
        override fun areItemsTheSame(oldItem: MenuItem, newItem: MenuItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MenuItem, newItem: MenuItem) =
            oldItem == newItem
    }
}
