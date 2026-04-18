//package com.example.hospedefact.ui.restauracion
//
//import android.app.AlertDialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Button
//import android.widget.ProgressBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.hospedefact.R
//import com.example.hospedefact.data.models.Mesa
//import com.example.hospedefact.ui.restauracion.MesaGridAdapter
//
///**
// * GestionarMesasFragment
// * Panel de gestión de mesas
// * Solo para gerentes
// */
//class GestionarMesasFragment : Fragment() {
//
//    private lateinit var viewModel: MesaViewModel
//    private lateinit var adapter: MesaGridAdapter
//
//    private lateinit var recyclerMesas: RecyclerView
//    private lateinit var btnAtras: Button
//    private lateinit var btnCrearMesas: Button
//    private lateinit var btnLimpiarTodo: Button
//    private lateinit var progressBar: ProgressBar
//    private lateinit var textVacio: TextView
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_gestionar_mesas, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewModel = MesaViewModel()
//
//        recyclerMesas = view.findViewById(R.id.recycler_mesas)
//        btnAtras = view.findViewById(R.id.btn_atras_mesas)
//        btnCrearMesas = view.findViewById(R.id.btn_crear_mesas_iniciales)
//        btnLimpiarTodo = view.findViewById(R.id.btn_limpiar_todas_mesas)
//        progressBar = view.findViewById(R.id.progress_bar_mesas)
//        textVacio = view.findViewById(R.id.text_vacio_mesas)
//
//        adapter = MesaGridAdapter { mesa ->
//            mostrarMenuMesa(mesa)
//        }
//
//        recyclerMesas.adapter = adapter
//        recyclerMesas.layoutManager = GridLayoutManager(context, 3)
//
//        btnAtras.setOnClickListener {
//            findNavController().navigate(R.id.action_admin_mesas_to_dashboard)
//        }
//
//        btnCrearMesas.setOnClickListener {
//            crearMesasIniciales()
//        }
//
//        btnLimpiarTodo.setOnClickListener {
//            mostrarDialogoLimpiarTodas()
//        }
//
//        cargarMesas()
//    }
//
//    private fun cargarMesas() {
//        viewModel.cargarMesas().observe(viewLifecycleOwner) { resultado ->
//            when (resultado) {
//                "cargando" -> {
//                    progressBar.visibility = View.VISIBLE
//                    recyclerMesas.visibility = View.GONE
//                    textVacio.visibility = View.GONE
//                }
//                is String -> {
//                    if (resultado.startsWith("error")) {
//                        progressBar.visibility = View.GONE
//                        recyclerMesas.visibility = View.GONE
//                        textVacio.visibility = View.VISIBLE
//                        textVacio.text = resultado
//                    }
//                }
//                is List<*> -> {
//                    @Suppress("UNCHECKED_CAST")
//                    val mesas = resultado as? List<Mesa> ?: emptyList()
//
//                    progressBar.visibility = View.GONE
//
//                    if (mesas.isEmpty()) {
//                        recyclerMesas.visibility = View.GONE
//                        textVacio.visibility = View.VISIBLE
//                        textVacio.text = "No hay mesas. Click en 'Crear Mesas Iniciales'"
//                        btnCrearMesas.visibility = View.VISIBLE
//                    } else {
//                        recyclerMesas.visibility = View.VISIBLE
//                        textVacio.visibility = View.GONE
//                        btnCrearMesas.visibility = View.GONE
//                        adapter.submitList(mesas.sortedBy { it.numero })
//                    }
//                }
//            }
//        }
//    }
//
//    private fun crearMesasIniciales() {
//        viewModel.crearMesasIniciales().observe(viewLifecycleOwner) { resultado ->
//            when (resultado) {
//                "exito" -> {
//                    Toast.makeText(context, "Mesas creadas exitosamente", Toast.LENGTH_SHORT).show()
//                    cargarMesas()
//                }
//                else -> {
//                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun mostrarMenuMesa(mesa: Mesa) {
//        val opciones = mutableListOf<String>()
//
//        when (mesa.estado) {
//            "disponible" -> {
//                opciones.add("Ocupar mesa")
//                opciones.add("Reservar")
//                opciones.add("Mantenimiento")
//            }
//            "ocupada" -> {
//                opciones.add("Liberar mesa")
//                opciones.add("Ver pedido")
//                opciones.add("Mantenimiento")
//            }
//            "reservada" -> {
//                opciones.add("Liberar reserva")
//                opciones.add("Ocupar mesa")
//            }
//            "mantenimiento" -> {
//                opciones.add("Disponible")
//            }
//        }
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Mesa ${mesa.numero}")
//            .setAdapter(
//                ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_list_item_1,
//                opciones
//            )
//            ) { _, which ->
//                when (opciones[which]) {
//                    "Ocupar mesa" -> ocuparMesa(mesa)
//                    "Liberar mesa" -> liberarMesa(mesa)
//                    "Reservar" -> cambiarEstado(mesa, "reservada")
//                    "Liberar reserva" -> cambiarEstado(mesa, "disponible")
//                    "Mantenimiento" -> cambiarEstado(mesa, "mantenimiento")
//                    "Disponible" -> cambiarEstado(mesa, "disponible")
//                    "Ver pedido" -> mostrarPedidoMesa(mesa)
//                }
//            }
//            .show()
//    }
//
//    private fun ocuparMesa(mesa: Mesa) {
//        Toast.makeText(context, "Ocupar mesa ${mesa.numero}", Toast.LENGTH_SHORT).show()
//        // Implementar selección de huésped y creación de pedido
//    }
//
//    private fun liberarMesa(mesa: Mesa) {
//        viewModel.liberarMesa(mesa.id).observe(viewLifecycleOwner) { resultado ->
//            when (resultado) {
//                "exito" -> {
//                    Toast.makeText(context, "Mesa ${mesa.numero} liberada", Toast.LENGTH_SHORT).show()
//                    cargarMesas()
//                }
//                else -> {
//                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun cambiarEstado(mesa: Mesa, nuevoEstado: String) {
//        viewModel.actualizarEstadoMesa(mesa.id, nuevoEstado).observe(viewLifecycleOwner) { resultado ->
//            when (resultado) {
//                "exito" -> {
//                    Toast.makeText(context, "Mesa ${mesa.numero} actualizada", Toast.LENGTH_SHORT).show()
//                    cargarMesas()
//                }
//                else -> {
//                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }
//
//    private fun mostrarPedidoMesa(mesa: Mesa) {
//        Toast.makeText(context, "Pedido de mesa ${mesa.numero}: ${mesa.pedidoId}", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun mostrarDialogoLimpiarTodas() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Limpiar todas las mesas")
//            .setMessage("Esto marcará todas las mesas como disponibles")
//            .setPositiveButton("Confirmar") { _, _ ->
//                // Implementar limpiar todas
//            }
//            .setNegativeButton("Cancelar", null)
//            .show()
//    }
//}