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
import com.example.hospedefact.R

/**
 * GestionarAlmacenFragment
 * Panel principal del sistema de almacén
 */
class GestionarAlmacenFragment : Fragment() {

    private lateinit var viewModel: ProductoAlmacenViewModel

    // Botones
    private lateinit var btnAtras: Button
    private lateinit var btnProductos: Button
    private lateinit var btnProveedores: Button
    private lateinit var btnOrdenes: Button
    private lateinit var btnAlertas: Button
    private lateinit var btnReportes: Button
    private lateinit var btnRecepcion: Button

    // Textos informativos
    private lateinit var textEstadisticas: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestionar_almacen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProductoAlmacenViewModel()

        // Obtiene referencias
        btnAtras = view.findViewById(R.id.btn_atras_almacen)
        btnProductos = view.findViewById(R.id.btn_productos)
        btnProveedores = view.findViewById(R.id.btn_proveedores)
        btnOrdenes = view.findViewById(R.id.btn_ordenes)
        btnAlertas = view.findViewById(R.id.btn_alertas)
        btnReportes = view.findViewById(R.id.btn_reportes)
        btnRecepcion = view.findViewById(R.id.btn_recepcion)
        textEstadisticas = view.findViewById(R.id.text_estadisticas)

        // Botón atrás
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_almacen_to_dashboard)
        }

        // Navegación
        btnProductos.setOnClickListener {
            findNavController().navigate(R.id.action_almacen_to_productos)
        }

        btnProveedores.setOnClickListener {
            findNavController().navigate(R.id.action_almacen_to_proveedores)
        }

        btnOrdenes.setOnClickListener {
            findNavController().navigate(R.id.action_almacen_to_ordenes)
        }

        btnAlertas.setOnClickListener {
            findNavController().navigate(R.id.action_almacen_to_alertas)
        }

        btnReportes.setOnClickListener {
            Toast.makeText(context, "Reportes en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnRecepcion.setOnClickListener {
            findNavController().navigate(R.id.action_almacen_to_recepcion)
        }

        // Cargar estadísticas
        cargarEstadisticas()
    }

    /**
     * Carga estadísticas del almacén
     */
    private fun cargarEstadisticas() {
        viewModel.cargarProductos().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val productos = resultado as? List<*> ?: emptyList<Any>()

                    val sb = StringBuilder()
                    sb.append("📊 ESTADÍSTICAS DEL ALMACÉN\n")
                    sb.append("═══════════════════════════════════════\n\n")
                    sb.append("📦 Total de productos: ${productos.size}\n")

                    // Contar productos con stock bajo
                    viewModel.obtenerProductosStockBajo().observe(viewLifecycleOwner) { bajos ->
                        when (bajos) {
                            is List<*> -> {
                                sb.append("⚠️ Productos con stock bajo: ${(bajos as? List<*>)?.size ?: 0}\n\n")
                            }
                        }
                    }

                    // Obtener valor total
                    viewModel.obtenerValorTotalInventario().observe(viewLifecycleOwner) { valor ->
                        when (valor) {
                            is Double -> {
                                sb.append("💰 Valor total inventario: €${String.format("%.2f", valor)}\n")
                                sb.append("═══════════════════════════════════════\n")
                                textEstadisticas.text = sb.toString()
                            }
                        }
                    }
                }
            }
        }
    }
}