package com.example.hospedefact.ui.almacen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.ProductoAlmacen

/**
 * AlertasStockFragment
 * Muestra productos con stock bajo para compra urgente
 */
class AlertasStockFragment : Fragment() {

    private lateinit var viewModel: ProductoAlmacenViewModel
    private lateinit var adapter: ProductoAlmacenAdapter

    // Vistas
    private lateinit var recyclerAlertas: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alertas_stock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProductoAlmacenViewModel()

        // Obtiene referencias
        recyclerAlertas = view.findViewById(R.id.recycler_alertas)
        btnAtras = view.findViewById(R.id.btn_atras_alertas)
        progressBar = view.findViewById(R.id.progress_bar_alertas)
        textVacio = view.findViewById(R.id.text_vacio_alertas)

        // Configura RecyclerView
        adapter = ProductoAlmacenAdapter(
            { producto ->
                // Crear orden de compra
            },
            { producto ->
                // Ver movimientos
            }
        )
        recyclerAlertas.adapter = adapter
        recyclerAlertas.layoutManager = LinearLayoutManager(context)

        // Botón atrás
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_alertas_to_almacen)
        }

        // Carga alertas
        cargarAlertas()
    }

    /**
     * Carga productos con stock bajo
     */
    private fun cargarAlertas() {
        viewModel.obtenerProductosStockBajo().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerAlertas.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerAlertas.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val productos = resultado as? List<ProductoAlmacen> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (productos.isEmpty()) {
                        recyclerAlertas.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "✅ Todos los productos con stock normal"
                    } else {
                        recyclerAlertas.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(productos)
                    }
                }
            }
        }
    }
}