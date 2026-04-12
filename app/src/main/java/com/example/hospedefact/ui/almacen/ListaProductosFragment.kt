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
import com.example.hospedefact.data.models.ProductoAlmacen

/**
 * ListaProductosFragment
 * Muestra todos los productos del almacén
 */
class ListaProductosFragment : Fragment() {

    private lateinit var viewModel: ProductoAlmacenViewModel
    private lateinit var adapter: ProductoAlmacenAdapter

    // Vistas
    private lateinit var recyclerProductos: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var btnNuevoProducto: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_productos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProductoAlmacenViewModel()

        // Obtiene referencias
        recyclerProductos = view.findViewById(R.id.recycler_productos)
        btnAtras = view.findViewById(R.id.btn_atras_productos)
        btnNuevoProducto = view.findViewById(R.id.btn_nuevo_producto)
        progressBar = view.findViewById(R.id.progress_bar_productos)
        textVacio = view.findViewById(R.id.text_vacio_productos)

        // Configura RecyclerView
        adapter = ProductoAlmacenAdapter(
            { producto ->
                Toast.makeText(context, "Editar: ${producto.nombre}", Toast.LENGTH_SHORT).show()
            },
            { producto ->
                Toast.makeText(context, "Ver movimientos: ${producto.nombre}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerProductos.adapter = adapter
        recyclerProductos.layoutManager = LinearLayoutManager(context)

        // Botones
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_productos_to_almacen)
        }

        btnNuevoProducto.setOnClickListener {
            findNavController().navigate(R.id.action_productos_to_nuevo_producto)
        }

        // Carga productos
        cargarProductos()
    }

    /**
     * Carga y muestra productos
     */
    private fun cargarProductos() {
        viewModel.cargarProductos().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerProductos.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerProductos.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val productos = resultado as? List<ProductoAlmacen> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (productos.isEmpty()) {
                        recyclerProductos.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay productos registrados"
                    } else {
                        recyclerProductos.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(productos)
                    }
                }
            }
        }
    }
}