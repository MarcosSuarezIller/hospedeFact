package com.example.hospedefact.ui.factura

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
import com.example.hospedefact.data.models.Mesa
import com.example.hospedefact.ui.restauracion.MesaViewModel
import com.google.firebase.firestore.FirebaseFirestore

class GenerarFacturaMesasFragment : Fragment() {

    private lateinit var spinnerMesa: Spinner
    private lateinit var recyclerPedidos: RecyclerView
    private lateinit var textResumen: TextView
    private lateinit var btnGenerar: Button
    private lateinit var btnAtras: Button
    private lateinit var progressBar: ProgressBar

    private var mesasOcupadas = mutableListOf<Mesa>()
    private var mesaSeleccionada: Mesa? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generar_factura_mesas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerMesa = view.findViewById(R.id.spinner_mesa_factura)
        recyclerPedidos = view.findViewById(R.id.recycler_pedidos_mesa_factura)
        textResumen = view.findViewById(R.id.text_resumen_mesa_factura)
        btnGenerar = view.findViewById(R.id.btn_generar_factura_mesa)
        btnAtras = view.findViewById(R.id.btn_atras_factura_mesas)
        progressBar = view.findViewById(R.id.progress_bar_factura_mesas)

        recyclerPedidos.layoutManager = LinearLayoutManager(context)

        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_factura_mesas_to_dashboard)
        }

        btnGenerar.setOnClickListener {
            generarFacturaMesa()
        }

        cargarMesasOcupadas()
    }

    private fun cargarMesasOcupadas() {
        val mesaViewModel = MesaViewModel()

        mesaViewModel.cargarMesasOcupadas().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    mesasOcupadas = (resultado as? List<Mesa>)?.toMutableList() ?: mutableListOf()

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        mesasOcupadas.map { "Mesa ${it.numero}" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerMesa.adapter = adapter

                    spinnerMesa.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            mesaSeleccionada = mesasOcupadas.getOrNull(position)
                            cargarPedidosMesa()
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                }
                else -> {
                    Toast.makeText(context, "No hay mesas ocupadas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cargarPedidosMesa() {
        val mesa = mesaSeleccionada ?: return

        db.collection("pedidos")
            .whereEqualTo("huespedId", mesa.id)
            .whereEqualTo("estado", "pendiente")
            .get()
            .addOnSuccessListener { snapshot ->
                val pedidos = snapshot.toObjects(com.example.hospedefact.data.models.Pedido::class.java)

                val total = pedidos.sumOf { it.total }
                val subtotal = total / 1.21
                val iva = total - subtotal

                textResumen.text = "Subtotal: €${String.format("%.2f", subtotal)}\n" +
                        "IVA (21%): €${String.format("%.2f", iva)}\n" +
                        "Total: €${String.format("%.2f", total)}"
            }
    }

    private fun generarFacturaMesa() {
        val mesa = mesaSeleccionada ?: return

        progressBar.visibility = View.VISIBLE

        db.collection("pedidos")
            .whereEqualTo("huespedId", mesa.id)
            .whereEqualTo("estado", "pendiente")
            .get()
            .addOnSuccessListener { snapshot ->
                val pedidos = snapshot.toObjects(com.example.hospedefact.data.models.Pedido::class.java)

                if (pedidos.isEmpty()) {
                    Toast.makeText(context, "No hay pedidos pendientes", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    return@addOnSuccessListener
                }

                val total = pedidos.sumOf { it.total }
                val iva = total * 0.21

                val factura = com.example.hospedefact.data.models.Factura(
                    huespedId = mesa.id,
                    items = pedidos.flatMap { it.items }.map { item ->
                        com.example.hospedefact.data.models.LineaFactura(
                            descripcion = item.nombre,
                            cantidad = item.cantidad,
                            precioUnitario = item.precioUnitario,
                            subtotal = item.cantidad * item.precioUnitario
                        )
                    },
                    subtotal = total / 1.21,
                    iva = iva,
                    total = total,
                    estado = "emitida"
                )

                val doc = db.collection("facturas").document()
                db.collection("facturas").document(doc.id).set(factura.copy(id = doc.id))
                    .addOnSuccessListener {
                        db.collection("pedidos")
                            .whereEqualTo("huespedId", mesa.id)
                            .whereEqualTo("estado", "pendiente")
                            .get()
                            .addOnSuccessListener { pedidosSnapshot ->
                                for (pedido in pedidosSnapshot) {
                                    db.collection("pedidos").document(pedido.id)
                                        .update("estado", "facturado")
                                }

                                progressBar.visibility = View.GONE
                                Toast.makeText(context, "Factura generada", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_factura_mesas_to_dashboard)
                            }
                    }
            }
    }
}