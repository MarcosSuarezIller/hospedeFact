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
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Huesped
import com.example.hospedefact.data.models.Pedido
import com.example.hospedefact.ui.huespedes.HuespedViewModel
import com.example.hospedefact.ui.pedidos.PedidoViewModel

/**
 * Fragmento encargado de orquestar la generación de facturas consolidadas.
 * Permite seleccionar un huésped, visualizar el desglose de sus consumos pendientes (pedidos)
 * y disparar el proceso de facturación legal que unifica todos los cargos.
 */
class GenerarFacturaFragment : Fragment() {

    private lateinit var viewModel: FacturaViewModel
    private lateinit var huespedViewModel: HuespedViewModel
    private lateinit var pedidoViewModel: PedidoViewModel

    // Vistas
    private lateinit var spinnerHuesped: Spinner
    private lateinit var btnAtras: Button
    private lateinit var btnGenerarFactura: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textResumen: TextView
    private lateinit var textSinPedidos: TextView

    private var huespedes = mutableListOf<Huesped>()
    private var huespedSeleccionado: Huesped? = null
    private var pedidosPendientes = mutableListOf<Pedido>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generar_factura, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = FacturaViewModel()
        huespedViewModel = HuespedViewModel()
        pedidoViewModel = PedidoViewModel()

        // Obtiene referencias
        spinnerHuesped = view.findViewById(R.id.spinner_huesped_factura)
        btnAtras = view.findViewById(R.id.btn_atras_factura)
        btnGenerarFactura = view.findViewById(R.id.btn_generar_factura)
        progressBar = view.findViewById(R.id.progress_bar_factura)
        textResumen = view.findViewById(R.id.text_resumen_pedidos)
        textSinPedidos = view.findViewById(R.id.text_sin_pedidos)

        // Botón atrás
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_factura_to_dashboard)
        }

        // Botón generar factura
        btnGenerarFactura.setOnClickListener {
            if (huespedSeleccionado != null && pedidosPendientes.isNotEmpty()) {
                generarFactura()
            } else {
                Toast.makeText(
                    context,
                    "Selecciona huésped con pedidos pendientes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Cargar huéspedes
        cargarHuespedes()
    }

    /**
     * Recupera y carga la lista de huéspedes desde el ViewModel para poblar el selector.
     * Al seleccionar un huésped, se desencadena automáticamente la búsqueda de sus pedidos pendientes.
     */
    private fun cargarHuespedes() {
        huespedViewModel.cargarHuespedes().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    huespedes = (resultado as? List<Huesped>)?.toMutableList() ?: mutableListOf()

                    // Crear adaptador para spinner
                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        huespedes.map { "${it.nombre} (${it.habitacion})" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerHuesped.adapter = adapter

                    // Listener para cuando selecciona huésped
                    spinnerHuesped.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            huespedSeleccionado = huespedes.getOrNull(position)
                            if (huespedSeleccionado != null) {
                                cargarPedidosPendientes(huespedSeleccionado!!.id)
                            }
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            huespedSeleccionado = null
                            mostrarSinPedidos()
                        }
                    }
                }
            }
        }
    }

    /**
     * Consulta al repositorio de pedidos aquellos registros que aún no han sido facturados
     * para el huésped seleccionado.
     * 
     * @param huespedId Identificador único del huésped.
     */
    private fun cargarPedidosPendientes(huespedId: String) {
        pedidoViewModel.obtenerPedidosPorHuesped(huespedId).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    pedidosPendientes = (resultado as? List<Pedido>)?.toMutableList() ?: mutableListOf()

                    if (pedidosPendientes.isEmpty()) {
                        mostrarSinPedidos()
                    } else {
                        mostrarResumen()
                    }
                }
            }
        }
    }

    /**
     * Construye y muestra una representación textual detallada de todos los pedidos pendientes.
     * Incluye desglose de ítems, cantidades, subtotales por pedido y el cálculo final de impuestos.
     */
    private fun mostrarResumen() {
        textSinPedidos.visibility = View.GONE
        textResumen.visibility = View.VISIBLE
        btnGenerarFactura.isEnabled = true

        // Construir texto de resumen
        val sb = StringBuilder()
        sb.append("📋 RESUMEN DE PEDIDOS\n")
        sb.append("═══════════════════════════════════════\n\n")

        var totalGeneral = 0.0

        for ((index, pedido) in pedidosPendientes.withIndex()) {
            sb.append("${index + 1}. Pedido #${pedido.id.take(8)}\n")
            sb.append("   Fecha: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(pedido.fecha)}\n")

            for (item in pedido.items) {
                val subtotal = item.cantidad * item.precioUnitario
                sb.append("   • ${item.nombre}: ${item.cantidad}x €${String.format("%.2f", item.precioUnitario)} = €${String.format("%.2f", subtotal)}\n")
            }

            sb.append("   Subtotal: €${String.format("%.2f", pedido.total)}\n\n")
            totalGeneral += pedido.total
        }

        sb.append("═══════════════════════════════════════\n")
        sb.append("Total antes de IVA: €${String.format("%.2f", totalGeneral)}\n")
        sb.append("IVA (21%): €${String.format("%.2f", totalGeneral * 0.21)}\n")
        sb.append("TOTAL CON IVA: €${String.format("%.2f", totalGeneral * 1.21)}\n")
        sb.append("═══════════════════════════════════════\n\n")
        sb.append("✅ Haz clic para generar factura consolidada")

        textResumen.text = sb.toString()
    }

    /**
     * Gestiona la visibilidad de la UI cuando no existen consumos pendientes de facturar.
     */
    private fun mostrarSinPedidos() {
        textResumen.visibility = View.GONE
        textSinPedidos.visibility = View.VISIBLE
        btnGenerarFactura.isEnabled = false
        textSinPedidos.text = "Sin pedidos pendientes"
    }

    /**
     * Ejecuta el proceso de consolidación de facturación.
     * Transforma todos los pedidos individuales en un único documento de [Factura]
     * y actualiza el estado de los mismos para evitar duplicidades.
     */
    private fun generarFactura() {
        val huesped = huespedSeleccionado ?: return

        viewModel.generarFactura(huesped.id).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    btnGenerarFactura.isEnabled = false
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        btnGenerarFactura.isEnabled = true
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    // Factura generada exitosamente
                    progressBar.visibility = View.GONE
                    btnGenerarFactura.isEnabled = true

                    Toast.makeText(context, "✅ Factura generada", Toast.LENGTH_SHORT).show()

                    // Navega a la vista de factura
                    val bundle = Bundle()
                    bundle.putString("factura_id", (resultado as? com.example.hospedefact.data.models.Factura)?.id ?: "")

                    // Limpia resumen
                    mostrarSinPedidos()
                }
            }
        }
    }
}