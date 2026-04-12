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
import com.example.hospedefact.data.models.Proveedor

/**
 * ListaProveedoresFragment
 * Muestra todos los proveedores
 */
class ListaProveedoresFragment : Fragment() {

    private lateinit var viewModel: ProveedorViewModel
    private lateinit var adapter: ProveedorAdapter

    // Vistas
    private lateinit var recyclerProveedores: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var btnNuevoProveedor: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_proveedores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProveedorViewModel()

        // Obtiene referencias
        recyclerProveedores = view.findViewById(R.id.recycler_proveedores)
        btnAtras = view.findViewById(R.id.btn_atras_proveedores)
        btnNuevoProveedor = view.findViewById(R.id.btn_nuevo_proveedor)
        progressBar = view.findViewById(R.id.progress_bar_proveedores)
        textVacio = view.findViewById(R.id.text_vacio_proveedores)

        // Configura RecyclerView
        adapter = ProveedorAdapter(
            { proveedor ->
                // Contactar
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(proveedor.email))
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Consulta desde HospedeFact")
                }
                startActivity(android.content.Intent.createChooser(intent, "Contactar proveedor"))
            },
            { proveedor ->
                // Eliminar
                viewModel.desactivarProveedor(proveedor.id).observe(viewLifecycleOwner) { resultado ->
                    if (resultado == "exito") {
                        Toast.makeText(context, "Proveedor desactivado", Toast.LENGTH_SHORT).show()
                        cargarProveedores()
                    }
                }
            }
        )
        recyclerProveedores.adapter = adapter
        recyclerProveedores.layoutManager = LinearLayoutManager(context)

        // Botones
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_proveedores_to_almacen)
        }

        btnNuevoProveedor.setOnClickListener {
            findNavController().navigate(R.id.action_proveedores_to_nuevo_proveedor)
        }

        // Carga proveedores
        cargarProveedores()
    }

    /**
     * Carga y muestra proveedores
     */
    private fun cargarProveedores() {
        viewModel.cargarProveedores().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerProveedores.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerProveedores.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val proveedores = resultado as? List<Proveedor> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (proveedores.isEmpty()) {
                        recyclerProveedores.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay proveedores registrados"
                    } else {
                        recyclerProveedores.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(proveedores)
                    }
                }
            }
        }
    }
}