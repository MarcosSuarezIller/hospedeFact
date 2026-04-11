package com.example.hospedefact.ui.factura

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
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Factura
import java.text.SimpleDateFormat

/**
 * VistaFacturaFragment
 * Muestra la factura generada completa
 */
class VistaFacturaFragment : Fragment() {

    private lateinit var viewModel: FacturaViewModel

    // Vistas
    private lateinit var btnAtras: Button
    private lateinit var btnMarcarPagada: Button
    private lateinit var btnCompartir: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textFactura: TextView

    private var facturaActual: Factura? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vista_factura, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = FacturaViewModel()

        // Obtiene referencias
        btnAtras = view.findViewById(R.id.btn_atras_vista)
        btnMarcarPagada = view.findViewById(R.id.btn_marcar_pagada)
        btnCompartir = view.findViewById(R.id.btn_compartir)
        progressBar = view.findViewById(R.id.progress_bar_vista)
        textFactura = view.findViewById(R.id.text_factura_contenido)

        // Botón atrás
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_vista_factura_to_dashboard)
        }

        // Botón marcar pagada
        btnMarcarPagada.setOnClickListener {
            if (facturaActual != null) {
                marcarComoPagada()
            }
        }

        // Botón compartir
        btnCompartir.setOnClickListener {
            if (facturaActual != null) {
                compartirFactura()
            }
        }

        // Obtiene ID de factura de los argumentos
        val facturaId = arguments?.getString("factura_id")

        if (!facturaId.isNullOrEmpty()) {
            cargarFactura(facturaId)
        } else {
            textFactura.text = "Error: No se especificó factura"
        }
    }

    /**
     * Carga y muestra la factura
     */
    private fun cargarFactura(facturaId: String) {
        viewModel.obtenerFactura(facturaId).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    textFactura.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        textFactura.visibility = View.VISIBLE
                        textFactura.text = resultado
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    val factura = resultado as? Factura
                    if (factura != null) {
                        facturaActual = factura
                        progressBar.visibility = View.GONE
                        textFactura.visibility = View.VISIBLE
                        mostrarFactura(factura)

                        // Actualizar estado del botón según estado de factura
                        if (factura.estado == "pagada") {
                            btnMarcarPagada.text = "✅ Pagada"
                            btnMarcarPagada.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    /**
     * Muestra la factura en formato legible
     */
    private fun mostrarFactura(factura: Factura) {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        sb.append("╔═════════════════════════════════════════╗\n")
        sb.append("║             HOSPEDACT FACTURA           ║\n")
        sb.append("╚═════════════════════════════════════════╝\n\n")

        sb.append("Factura #${factura.id.take(8).uppercase()}\n")
        sb.append("Fecha: ${dateFormat.format(factura.fechaEmision)}\n")
        sb.append("Estado: ${if (factura.estado == "pagada") "✅ PAGADA" else "⏳ EMITIDA"}\n\n")

        sb.append("─────────────────────────────────────────\n")
        sb.append("DETALLES DE SERVICIOS\n")
        sb.append("─────────────────────────────────────────\n\n")

        for (item in factura.items) {
            sb.append("${item.descripcion}\n")
            sb.append("  ${item.cantidad}x €${String.format("%.2f", item.precioUnitario)} = ")
            sb.append("€${String.format("%.2f", item.subtotal)}\n\n")
        }

        sb.append("═════════════════════════════════════════\n")
        sb.append("Subtotal:           €${String.format("%.2f", factura.subtotal)}\n")
        sb.append("IVA (21%):          €${String.format("%.2f", factura.iva)}\n")
        sb.append("─────────────────────────────────────────\n")
        sb.append("TOTAL A PAGAR:     €${String.format("%.2f", factura.total)}\n")
        sb.append("═════════════════════════════════════════\n\n")

        sb.append("Generada por: ${factura.generadaPor}\n")

        textFactura.text = sb.toString()
    }

    /**
     * Marca la factura como pagada
     */
    private fun marcarComoPagada() {
        val factura = facturaActual ?: return

        viewModel.marcarComoPagada(factura.id).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "exito" -> {
                    Toast.makeText(context, "✅ Factura marcada como pagada", Toast.LENGTH_SHORT).show()
                    facturaActual = facturaActual?.copy(estado = "pagada")
                    mostrarFactura(facturaActual!!)
                    btnMarcarPagada.text = "✅ Pagada"
                    btnMarcarPagada.isEnabled = false
                }
                else -> {
                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Comparte la factura por email/whatsapp/etc
     */
    private fun compartirFactura() {
        val factura = facturaActual ?: return

        val contenido = """
            FACTURA #${factura.id.take(8)}
            
            Total: €${String.format("%.2f", factura.total)}
            
            Subtotal: €${String.format("%.2f", factura.subtotal)}
            IVA (21%): €${String.format("%.2f", factura.iva)}
        """.trimIndent()

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, contenido)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Factura #${factura.id.take(8)}")
        }

        startActivity(android.content.Intent.createChooser(intent, "Compartir factura"))
    }
}