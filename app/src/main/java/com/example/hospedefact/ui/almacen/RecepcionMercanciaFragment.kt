package com.example.hospedefact.ui.almacen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.OrdenCompra

/**
 * RecepcionMercanciaFragment
 * Panel para recibir órdenes de compra
 * ⭐ CORE: Actualiza stock automáticamente
 */
class RecepcionMercanciaFragment : Fragment() {

    private lateinit var ordenViewModel: OrdenCompraViewModel
    private lateinit var adapter: OrdenCompraAdapter

    // Vistas
    private lateinit var recyclerOrdenes: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recepcion_mercancia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ordenViewModel = OrdenCompraViewModel()

        // Obtiene referencias
        recyclerOrdenes = view.findViewById(R.id.recycler_ordenes_recepcion)
        btnAtras = view.findViewById(R.id.btn_atras_recepcion)
        progressBar = view.findViewById(R.id.progress_bar_recepcion)
        textVacio = view.findViewById(R.id.text_vacio_recepcion)

        // Configura RecyclerView
        adapter = OrdenCompraAdapter(
            { orden ->
                Toast.makeText(context, "Detalles: ${orden.id.take(8)}", Toast.LENGTH_SHORT).show()
            },
            { orden ->
                // ⭐ RECIBIR MERCANCÍA
                mostrarDialogoRecepcion(orden)
            }
        )
        recyclerOrdenes.adapter = adapter
        recyclerOrdenes.layoutManager = LinearLayoutManager(context)

        // Botón atrás
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_recepcion_to_almacen)
        }

        // Carga órdenes confirmadas (listas para recibir)
        cargarOrdenesConfirmadas()
    }

    /**
     * Carga órdenes confirmadas que están esperando recepción
     */
    private fun cargarOrdenesConfirmadas() {
        ordenViewModel.obtenerOrdenesPendientes().observe(viewLifecycleOwner) { resultado ->
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
                    val ordenes = (resultado as? List<OrdenCompra>)?.filter {
                        it.estado == "confirmada"
                    } ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (ordenes.isEmpty()) {
                        recyclerOrdenes.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay órdenes confirmadas para recibir"
                    } else {
                        recyclerOrdenes.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(ordenes.toList())
                    }
                }
            }
        }
    }

    /**
     * Muestra diálogo para recibir mercancía
     */
    private fun mostrarDialogoRecepcion(orden: OrdenCompra) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_recepcion_mercancia, null)

        val textOrdenId = view.findViewById<TextView>(R.id.text_orden_id_recepcion)
        val textProveedor = view.findViewById<TextView>(R.id.text_proveedor_recepcion)
        val recyclerItems = view.findViewById<RecyclerView>(R.id.recycler_items_recepcion)
        val btnCancelar = view.findViewById<Button>(R.id.btn_cancelar_recepcion)
        val btnConfirmar = view.findViewById<Button>(R.id.btn_confirmar_recepcion)

        // Muestra info de orden
        textOrdenId.text = "Orden #${orden.id.take(8).uppercase()}"
        textProveedor.text = "Proveedor: ${orden.proveedorNombre}"

        // Copia mutable de los items para actualizar cantidades
        val itemsEditables = orden.items.toMutableList()

        // Configura RecyclerView con items para confirmar cantidad
        val itemAdapter = ItemRecepcionAdapter { item, cantidadRecibida ->
            // Actualizar cantidad recibida en la lista mutable
            val index = itemsEditables.indexOfFirst { it.productoId == item.productoId }
            if (index != -1) {
                itemsEditables[index] = item.copy(cantidadRecibida = cantidadRecibida)
            }
        }
        recyclerItems.adapter = itemAdapter
        recyclerItems.layoutManager = LinearLayoutManager(context)
        itemAdapter.submitList(orden.items)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            // ⭐ RECIBIR MERCANCÍA CON CANTIDADES ACTUALES
            recibirMercancia(orden.copy(items = itemsEditables))
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * ⭐ RECIBE LA MERCANCÍA Y ACTUALIZA STOCK
     */
    private fun recibirMercancia(orden: OrdenCompra) {
        ordenViewModel.recibirMercancia(orden.id).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    Toast.makeText(context, "Procesando...", Toast.LENGTH_SHORT).show()
                }
                "exito" -> {
                    Toast.makeText(context, "✅ Mercancía recibida y stock actualizado", Toast.LENGTH_LONG).show()

                    // Recarga la lista
                    cargarOrdenesConfirmadas()
                }
                else -> {
                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}