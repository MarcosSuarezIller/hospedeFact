package com.example.hospedefact.ui.factura

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Huesped
import com.example.hospedefact.data.models.Pedido
import com.example.hospedefact.ui.huespedes.HuespedViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class GenerarFacturaConEstanciaFragment : Fragment() {

    private lateinit var spinnerHuesped: Spinner
    private lateinit var inputFechaSalida: EditText
    private lateinit var textResumen: TextView
    private lateinit var textDetalleEstancia: TextView
    private lateinit var btnGenerar: Button
    private lateinit var btnAtras: Button
    private lateinit var progressBar: ProgressBar

    private var huespedes = mutableListOf<Huesped>()
    private var huespedSeleccionado: Huesped? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generar_factura_estancia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerHuesped = view.findViewById(R.id.spinner_huesped_estancia)
        inputFechaSalida = view.findViewById(R.id.input_fecha_salida)
        textResumen = view.findViewById(R.id.text_resumen_estancia)
        textDetalleEstancia = view.findViewById(R.id.text_detalle_estancia)
        btnGenerar = view.findViewById(R.id.btn_generar_factura_estancia)
        btnAtras = view.findViewById(R.id.btn_atras_factura_estancia)
        progressBar = view.findViewById(R.id.progress_bar_factura_estancia)

        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_factura_estancia_to_dashboard)
        }

        btnGenerar.setOnClickListener {
            generarFacturaEstancia()
        }

        cargarHuespedes()
    }

    private fun cargarHuespedes() {
        val huespedViewModel = HuespedViewModel()

        huespedViewModel.cargarHuespedes().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    huespedes = (resultado as? List<Huesped>)?.filter { it.estado == "activo" }
                        ?.toMutableList() ?: mutableListOf()

                    if (huespedes.isEmpty()) {
                        Toast.makeText(context, "No hay huespedes activos", Toast.LENGTH_SHORT)
                            .show()
                        return@observe
                    }

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        huespedes.map { "${it.nombre} - ${it.habitacion}" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerHuesped.adapter = adapter

                    spinnerHuesped.onItemSelectedListener =
                        object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: android.widget.AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                huespedSeleccionado = huespedes.getOrNull(position)
                                cargarDetallesHuesped()
                            }

                            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                        }
                }
            }
        }
    }



    private fun cargarDetallesHuesped() {
        val huesped = huespedSeleccionado ?: return
        Log.d("GenerarFacturaConEstanciaFragment", "Huesped seleccionado: ${huesped.nombre}, Precio noche: ${huesped.precioNocheHabitacion}")

        if (huesped.habitacionId.isEmpty()) {
            Toast.makeText(context, "Error: El huesped no tiene una habitación asignada", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("habitaciones")
            .document(huesped.habitacionId)
            .get()
            .addOnSuccessListener { doc ->
                val habitacion =
                    doc.toObject(com.example.hospedefact.data.models.Habitacion::class.java)

                if (habitacion != null) {
                    val precioNoche = habitacion.precioNoche
                    val diasEstancia =
                        calcularDiasEstancia(huesped.fechaEntrada, System.currentTimeMillis())
                    val costoEstancia = diasEstancia * precioNoche

                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val fechaEntradaStr = sdf.format(java.util.Date(huesped.fechaEntrada))
                    val fechaHoy = sdf.format(java.util.Date(System.currentTimeMillis()))

                    textDetalleEstancia.text = "Entrada: $fechaEntradaStr\n" +
                            "Hoy: $fechaHoy\n" +
                            "Dias: $diasEstancia\n" +
                            "Precio noche: €${String.format("%.2f", precioNoche)}\n" +
                            "Subtotal estancia: €${String.format("%.2f", costoEstancia)}"

                    db.collection("pedidos")
                        .whereEqualTo("huespedId", huesped.id)
                        .whereEqualTo("estado", "pendiente")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val pedidos = snapshot.toObjects(Pedido::class.java)

                            var subtotalPedidos = 0.0

                            for (pedido in pedidos) {
                                for (item in pedido.items) {
                                    subtotalPedidos += item.cantidad * item.precioUnitario
                                }
                            }

                            val subtotal = subtotalPedidos + costoEstancia
                            val iva = subtotal * 0.21
                            val total = subtotal + iva

                            textResumen.text =
                                "Pedidos: €${String.format("%.2f", subtotalPedidos)}\n" +
                                        "Estancia: €${String.format("%.2f", costoEstancia)}\n" +
                                        "Subtotal: €${String.format("%.2f", subtotal)}\n" +
                                        "IVA (21%): €${String.format("%.2f", iva)}\n" +
                                        "Total: €${String.format("%.2f", total)}"
                        }
                }
            }
    }


    private fun calcularDiasEstancia(fechaEntrada: Long, fechaSalida: Long): Int {
        val dias = ((fechaSalida - fechaEntrada) / (1000 * 60 * 60 * 24)).toInt()
        return dias.coerceAtLeast(1)
    }


    private fun generarFacturaEstancia() {
        val huesped = huespedSeleccionado ?: return

        progressBar.visibility = View.VISIBLE

        if (huesped.habitacionId.isEmpty()) {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Error: El huesped no tiene una habitación asignada", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("habitaciones")
            .document(huesped.habitacionId)
            .get()
            .addOnSuccessListener { doc ->
                val habitacion =
                    doc.toObject(com.example.hospedefact.data.models.Habitacion::class.java)

                if (habitacion == null) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: Habitacion no encontrada", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }

                val precioNoche = habitacion.precioNoche
                val diasEstancia =
                    calcularDiasEstancia(huesped.fechaEntrada, System.currentTimeMillis())
                val costoEstancia = diasEstancia * precioNoche

                db.collection("pedidos")
                    .whereEqualTo("huespedId", huesped.id)
                    .whereEqualTo("estado", "pendiente")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val pedidos = snapshot.toObjects(Pedido::class.java)

                        val items =
                            mutableListOf<com.example.hospedefact.data.models.LineaFactura>()

                        var subtotalPedidos = 0.0

                        for (pedido in pedidos) {
                            for (item in pedido.items) {
                                val subtotalItem = item.cantidad * item.precioUnitario
                                items.add(
                                    com.example.hospedefact.data.models.LineaFactura(
                                        descripcion = item.nombre,
                                        cantidad = item.cantidad,
                                        precioUnitario = item.precioUnitario,
                                        subtotal = subtotalItem
                                    )
                                )
                                subtotalPedidos += subtotalItem
                            }
                        }

                        items.add(
                            com.example.hospedefact.data.models.LineaFactura(
                                descripcion = "Estancia habitacion ${diasEstancia} noche(s)",
                                cantidad = diasEstancia,
                                precioUnitario = precioNoche,
                                subtotal = costoEstancia
                            )
                        )

                        val subtotal = subtotalPedidos + costoEstancia
                        val iva = subtotal * 0.21
                        val total = subtotal + iva

                        val factura = com.example.hospedefact.data.models.Factura(
                            huespedId = huesped.id,
                            nombreHuesped = huesped.nombre,
                            items = items,
                            subtotal = subtotal,
                            iva = iva,
                            total = total,
                            estado = "emitida",
                            incluyelEstancia = true,
                            diasEstancia = diasEstancia,
                            costoEstancia = costoEstancia
                        )

                        val doc = db.collection("facturas").document()
                        db.collection("facturas").document(doc.id).set(factura.copy(id = doc.id))
                            .addOnSuccessListener {
                                db.collection("pedidos")
                                    .whereEqualTo("huespedId", huesped.id)
                                    .whereEqualTo("estado", "pendiente")
                                    .get()
                                    .addOnSuccessListener { pedidosSnapshot ->
                                        for (pedido in pedidosSnapshot) {
                                            db.collection("pedidos").document(pedido.id)
                                                .update("estado", "facturado")
                                        }

                                        progressBar.visibility = View.GONE
                                        Toast.makeText(
                                            context,
                                            "Factura con estancia generada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        findNavController().navigate(R.id.action_factura_estancia_to_dashboard)
                                    }
                            }
                    }
            }
    }
}