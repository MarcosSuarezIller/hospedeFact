//package com.example.hospedefact.ui.almacen
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hospedefact.R
//import com.example.hospedefact.data.models.OrdenCompra
//
///**
// * ListaOrdenesCompraFragment
// * Muestra todas las órdenes de compra
// */
//class ListaOrdenesCompraFragment : Fragment() {
//
//    private lateinit var viewModel: OrdenCompraViewModel
//    private lateinit var adapter: OrdenCompraAdapter
//
//    // Vistas
//    private lateinit var recyclerOrdenes: RecyclerView
//    private lateinit var btnAtras: Button
//    private lateinit var btnNuevaOrden: Button
//    private lateinit var progressBar: ProgressBar
//    private lateinit var textVacio: TextView
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_lista_ordenes_compra, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel = OrdenCompraViewModel()
//
//        // Obtiene referencias
//        recyclerOrdenes = view.findViewById(R.id.recycler_ordenes)
//        btnAtras = view.findViewById(R.id.btn_atras_ordenes)
//        btnNuevaOrden = view.findViewById(R.id.btn_nueva_orden)
//        progressBar = view.findViewById(R.id.progress_bar_ordenes)
//        textVacio = view.findViewById(R.id.text_vacio_ordenes)
//
//        // Configura RecyclerView
//        adapter = OrdenCompraAdapter(
//            { orden ->
//                Toast.makeText(context, "Detalles de orden: ${orden.id.take(8)}", Toast.LENGTH_SHORT).show()
//                mostrarDetallesOrden(orden)
//            },
//            { orden ->
//                    // CONFIRMAR ORDEN (para que aparezca en recepción)
////                    if (orden.estado == "pendiente") {
////                        mostrarDialogoConfirmarOrden(orden)
////                    } else {
////                        Toast.makeText(context, "Orden ya está confirmada", Toast.LENGTH_SHORT).show()
////                    }
//                viewModel.recibirMercancia(orden.id).observe(viewLifecycleOwner) { resultado ->
//                    when (resultado) {
//                        "exito" -> {
//                            Toast.makeText(context, "✅ Mercancía recibida", Toast.LENGTH_SHORT).show()
//                            cargarOrdenes()
//                        }
//                        else -> {
//                            Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            }
//        )
//        recyclerOrdenes.adapter = adapter
//        recyclerOrdenes.layoutManager = LinearLayoutManager(context)
//
//        // Botones
//        btnAtras.setOnClickListener {
//            findNavController().navigate(R.id.action_ordenes_to_almacen)
//        }
//
//        btnNuevaOrden.setOnClickListener {
//            findNavController().navigate(R.id.action_ordenes_to_crear_orden)
//        }
//
//        // Carga órdenes
//        cargarOrdenes()
//    }
//
//    /**
//     * Muestra diálogo para confirmar orden
//     */
//    private fun mostrarDialogoConfirmarOrden(orden: OrdenCompra) {
//        android.app.AlertDialog.Builder(requireContext())
//            .setTitle("Confirmar Orden")
//            .setMessage("¿Confirmar la orden con el proveedor?\n\nUna vez confirmada, podrá recibir mercancía.")
//            .setPositiveButton("✅ Confirmar") { _, _ ->
//                confirmarOrden(orden)
//            }
//            .setNegativeButton("Cancelar", null)
//            .show()
//    }
//
//    /**
//     * Confirma la orden
//     */
//    private fun confirmarOrden(orden: OrdenCompra) {
//        viewModel.cambiarEstadoOrden(orden.id, "confirmada")
//            .observe(viewLifecycleOwner) { resultado ->
//                when (resultado) {
//                    "exito" -> {
//                        Toast.makeText(context, "✅ Orden confirmada. Puede recibir en Recepción.", Toast.LENGTH_LONG).show()
//                        cargarOrdenes()
//                    }
//                    else -> {
//                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//    }
//
//    /**
//     * Carga y muestra órdenes pendientes
//     */
//    private fun cargarOrdenes() {
//        viewModel.obtenerOrdenesPendientes().observe(viewLifecycleOwner) { resultado ->
//            when (resultado) {
//                "cargando" -> {
//                    progressBar.visibility = View.VISIBLE
//                    recyclerOrdenes.visibility = View.GONE
//                    textVacio.visibility = View.GONE
//                }
//                is String -> {
//                    if (resultado.startsWith("error")) {
//                        progressBar.visibility = View.GONE
//                        recyclerOrdenes.visibility = View.GONE
//                        textVacio.visibility = View.VISIBLE
//                        textVacio.text = resultado
//                    }
//                }
//                is List<*> -> {
//                    @Suppress("UNCHECKED_CAST")
//                    val ordenes = resultado as? List<OrdenCompra> ?: emptyList()
//
//                    progressBar.visibility = View.GONE
//
//                    if (ordenes.isEmpty()) {
//                        recyclerOrdenes.visibility = View.GONE
//                        textVacio.visibility = View.VISIBLE
//                        textVacio.text = "No hay órdenes pendientes"
//                    } else {
//                        recyclerOrdenes.visibility = View.VISIBLE
//                        textVacio.visibility = View.GONE
//                        adapter.submitList(ordenes)
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Muestra diálogo con detalles de la orden
//     */
//    private fun mostrarDetallesOrden(orden: OrdenCompra) {
//        android.util.Log.d("ListaOrdenesCompraFragment", "Abriendo diálogo de detalles")
//
//        val inflater = LayoutInflater.from(context)
//        val view = inflater.inflate(R.layout.dialog_detalles_orden, null)
//
//        val textOrdenId = view.findViewById<TextView>(R.id.text_orden_id_detalles)
//        val textProveedor = view.findViewById<TextView>(R.id.text_proveedor_detalles)
//        val textFecha = view.findViewById<TextView>(R.id.text_fecha_detalles)
//        val recyclerItems = view.findViewById<RecyclerView>(R.id.recycler_items_detalles)
//        val textTotal = view.findViewById<TextView>(R.id.text_total_detalles)
//        val btnCerrar = view.findViewById<Button>(R.id.btn_cerrar_detalles)
//
//        // Llena datos
//        textOrdenId.text = "Orden #${orden.id.take(8).uppercase()}"
//        textProveedor.text = "Proveedor: ${orden.proveedorNombre}"
//
//        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
//        textFecha.text = "Fecha: ${dateFormat.format(orden.fecha)}"
//
//        // Configura RecyclerView de items
//        val itemAdapter = DetallesOrdenAdapter()
//        recyclerItems.adapter = itemAdapter
//        recyclerItems.layoutManager = LinearLayoutManager(context)
//        itemAdapter.submitList(orden.items)
//
//        // Total
//        val subtotal = orden.total / 1.21  // Quitar IVA
//        val iva = orden.total - subtotal
//        textTotal.text = "Subtotal: €${String.format("%.2f", subtotal)}\n" +
//                "IVA (21%): €${String.format("%.2f", iva)}\n" +
//                "Total: €${String.format("%.2f", orden.total)}"
//
//        val dialog = android.app.AlertDialog.Builder(requireContext())
//            .setView(view)
//            .setCancelable(true)
//            .create()
//
//        btnCerrar.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        dialog.show()
//        android.util.Log.d("ListaOrdenesCompraFragment", "Diálogo mostrado")
//    }
//}
package com.example.hospedefact.ui.almacen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.OrdenCompra
import java.text.SimpleDateFormat

/**
 * ListaOrdenesCompraFragment
 * Muestra todas las órdenes de compra con opciones de cambiar estado
 */
class ListaOrdenesCompraFragment : Fragment() {

    private lateinit var viewModel: OrdenCompraViewModel
    private lateinit var adapter: OrdenCompraAdapterConAcciones

    // Vistas
    private lateinit var recyclerOrdenes: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var btnNuevaOrden: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_ordenes_compra, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = OrdenCompraViewModel()

        // Obtiene referencias
        recyclerOrdenes = view.findViewById(R.id.recycler_ordenes)
        btnAtras = view.findViewById(R.id.btn_atras_ordenes)
        btnNuevaOrden = view.findViewById(R.id.btn_nueva_orden)
        progressBar = view.findViewById(R.id.progress_bar_ordenes)
        textVacio = view.findViewById(R.id.text_vacio_ordenes)

        // Configura RecyclerView con nuevo adaptador
        adapter = OrdenCompraAdapterConAcciones(
            onDetallesClick = { orden ->
                mostrarDetallesOrden(orden)
            },
            onCambiarEstadoClick = { orden ->
                mostrarOpcionesEstado(orden)
            },
            onRecibirClick = { orden ->
                // RECIBIR MERCANCÍA
                viewModel.recibirMercancia(orden.id).observe(viewLifecycleOwner) { resultado ->
                    when (resultado) {
                        "exito" -> {
                            Toast.makeText(context, "✅ Mercancía recibida", Toast.LENGTH_SHORT).show()
                            cargarOrdenes()
                        }
                        else -> {
                            Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
        recyclerOrdenes.adapter = adapter
        recyclerOrdenes.layoutManager = LinearLayoutManager(context)

        // Botones
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_ordenes_to_almacen)
        }

        btnNuevaOrden.setOnClickListener {
            findNavController().navigate(R.id.action_ordenes_to_crear_orden)
        }

        // Carga órdenes
        cargarOrdenes()
    }

    /**
     * Carga todas las órdenes
     */
    private fun cargarOrdenes() {
        viewModel.obtenerTodasOrdenes().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerOrdenes.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerOrdenes.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val ordenes = resultado as? List<OrdenCompra> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (ordenes.isEmpty()) {
                        recyclerOrdenes.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay órdenes de compra"
                    } else {
                        recyclerOrdenes.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        // Ordena por fecha más reciente
                        val ordenesOrdenadas = ordenes.sortedByDescending { it.fecha }
                        adapter.submitList(ordenesOrdenadas)
                    }
                }
            }
        }
    }

    /**
     * Muestra diálogo con detalles de la orden
     */
    private fun mostrarDetallesOrden(orden: OrdenCompra) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_detalles_orden, null)

        val textOrdenId = view.findViewById<TextView>(R.id.text_orden_id_detalles)
        val textProveedor = view.findViewById<TextView>(R.id.text_proveedor_detalles)
        val textFecha = view.findViewById<TextView>(R.id.text_fecha_detalles)
        val recyclerItems = view.findViewById<RecyclerView>(R.id.recycler_items_detalles)
        val textTotal = view.findViewById<TextView>(R.id.text_total_detalles)
        val btnCerrar = view.findViewById<Button>(R.id.btn_cerrar_detalles)

        // Llena datos
        textOrdenId.text = "Orden #${orden.id.take(8).uppercase()}"
        textProveedor.text = "Proveedor: ${orden.proveedorNombre}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        textFecha.text = "Fecha: ${dateFormat.format(orden.fecha)}"

        // Configura RecyclerView
        val itemAdapter = DetallesOrdenAdapter()
        recyclerItems.adapter = itemAdapter
        recyclerItems.layoutManager = LinearLayoutManager(context)
        itemAdapter.submitList(orden.items)

        // Total
        val subtotal = orden.total / 1.21
        val iva = orden.total - subtotal
        textTotal.text = "Subtotal: €${String.format("%.2f", subtotal)}\n" +
                "IVA (21%): €${String.format("%.2f", iva)}\n" +
                "Total: €${String.format("%.2f", orden.total)}"

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Muestra opciones para cambiar el estado de la orden
     */
    private fun mostrarOpcionesEstado(orden: OrdenCompra) {
        val opciones = mutableListOf<String>()

        // Según el estado actual, mostrar opciones disponibles
        when (orden.estado) {
            "pendiente" -> {
                opciones.add("✅ Confirmar (pendiente → confirmada)")
                opciones.add("❌ Cancelar Orden")
            }
            "confirmada" -> {
                opciones.add("✔️ Marcar Entregada (confirmada → entregada)")
                opciones.add("❌ Cancelar Orden")
            }
            "entregada" -> {
                opciones.add("⏮️ Volver a Pendiente")
            }
            "cancelada" -> {
                opciones.add("♻️ Reactivar Orden")
            }
        }

        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            opciones
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Cambiar Estado - Orden #${orden.id.take(8)}")
            .setAdapter(adapter) { _, which ->
                val opcionSeleccionada = opciones[which]
                when {
                    opcionSeleccionada.contains("Confirmar") -> {
                    cambiarEstado(orden, "confirmada")
                }
                    opcionSeleccionada.contains("Entregada") -> {
                    cambiarEstado(orden, "entregada")
                }
                    opcionSeleccionada.contains("Cancelar") -> {
                    cambiarEstado(orden, "cancelada")
                }
                    opcionSeleccionada.contains("Volver a") -> {
                    cambiarEstado(orden, "pendiente")
                }
                    opcionSeleccionada.contains("Reactivar") -> {
                    cambiarEstado(orden, "pendiente")
                }
                }
            }
            .show()
    }

    /**
     * Cambia el estado de una orden
     */
    private fun cambiarEstado(orden: OrdenCompra, nuevoEstado: String) {
        viewModel.cambiarEstadoOrden(orden.id, nuevoEstado)
            .observe(viewLifecycleOwner) { resultado ->
                when (resultado) {
                    "cargando" -> {
                        Toast.makeText(context, "Cambiando estado...", Toast.LENGTH_SHORT).show()
                    }
                    "exito" -> {
                        Toast.makeText(
                            context,
                            "✅ Estado actualizado: ${orden.estado} → $nuevoEstado",
                            Toast.LENGTH_LONG
                        ).show()
                        cargarOrdenes()
                    }
                    else -> {
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
