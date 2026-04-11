package com.example.hospedefact.ui.pedidos

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
import com.example.hospedefact.data.models.Huesped
import com.example.hospedefact.data.models.ItemPedido
import com.example.hospedefact.data.models.MenuItem
import com.example.hospedefact.ui.huespedes.HuespedViewModel

/**
 * CrearPedidoFragment
 * Permite crear pedidos para un huésped
 * Incluye menú, carrito y cálculo de totales
 */
class CrearPedidoFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var huespedViewModel: HuespedViewModel

    private lateinit var spinnerHuesped: Spinner
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var recyclerCarrito: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var btnCrearPedido: Button
    private lateinit var textTotal: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var menuAdapter: MenuAdapter
    private lateinit var carritoAdapter: CarritoAdapter

    private var huespedes = mutableListOf<Huesped>()
    private var huespedSeleccionado: Huesped? = null
    private var carrito = mutableListOf<ItemPedido>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_pedido, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = PedidoViewModel()
        huespedViewModel = HuespedViewModel()

        // Obtiene referencias
        spinnerHuesped = view.findViewById(R.id.spinner_huesped)
        recyclerMenu = view.findViewById(R.id.recycler_menu)
        recyclerCarrito = view.findViewById(R.id.recycler_carrito)
        btnAtras = view.findViewById(R.id.btn_atras_pedido)
        btnCrearPedido = view.findViewById(R.id.btn_crear_pedido)
        textTotal = view.findViewById(R.id.text_total)
        progressBar = view.findViewById(R.id.progress_bar_pedido)

        // Configura RecyclerViews
        menuAdapter = MenuAdapter { item ->
            agregarAlCarrito(item)
        }
        recyclerMenu.adapter = menuAdapter
        recyclerMenu.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        carritoAdapter = CarritoAdapter(
            { itemActualizado, nueva ->
                actualizarCarrito(itemActualizado)
            },
            { itemAEliminar ->
                eliminarDelCarrito(itemAEliminar)
            }
        )
        recyclerCarrito.adapter = carritoAdapter
        recyclerCarrito.layoutManager = LinearLayoutManager(context)

        // Botón atrás
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_pedidos_to_dashboard)
        }

        // Botón crear pedido
        btnCrearPedido.setOnClickListener {
            if (huespedSeleccionado != null && carrito.isNotEmpty()) {
                crearPedido()
            } else {
                Toast.makeText(context, "Selecciona huésped y agrega items", Toast.LENGTH_SHORT).show()
            }
        }

        // Carga huéspedes
        cargarHuespedes()

        // Carga menú
        cargarMenu()
    }

    /**
     * Carga lista de huéspedes para el spinner
     */
    private fun cargarHuespedes() {
        huespedViewModel.cargarHuespedes().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    huespedes = (resultado as? List<Huesped>)?.toMutableList() ?: mutableListOf()

                    // Crea adaptador para spinner
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
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            huespedSeleccionado = null
                        }
                    }
                }
            }
        }
    }

    /**
     * Carga menú desde Firestore
     */
    private fun cargarMenu() {
        viewModel.cargarMenu().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> progressBar.visibility = View.VISIBLE
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
                is List<*> -> {
                    progressBar.visibility = View.GONE
                    @Suppress("UNCHECKED_CAST")
                    val items = resultado as? List<MenuItem> ?: emptyList()
                    menuAdapter.submitList(items)
                }
            }
        }
    }

    /**
     * Agrega item al carrito
     */
    private fun agregarAlCarrito(item: MenuItem) {
        val itemExistente = carrito.find { it.itemId == item.id }

        if (itemExistente != null) {
            // Si ya existe, aumenta cantidad
            val itemActualizado = itemExistente.copy(cantidad = itemExistente.cantidad + 1)
            carrito.remove(itemExistente)
            carrito.add(itemActualizado)
        } else {
            // Si no existe, crea nuevo
            carrito.add(
                ItemPedido(
                    itemId = item.id,
                    nombre = item.nombre,
                    cantidad = 1,
                    precioUnitario = item.precio
                )
            )
        }

        actualizarCarrito(null)
        Toast.makeText(context, "✅ ${item.nombre} agregado", Toast.LENGTH_SHORT).show()
    }

    /**
     * Actualiza vista del carrito
     */
    private fun actualizarCarrito(itemActualizado: ItemPedido?) {
        if (itemActualizado != null) {
            val index = carrito.indexOfFirst { it.itemId == itemActualizado.itemId }
            if (index != -1) {
                carrito[index] = itemActualizado
            }
        }

        carritoAdapter.submitList(carrito.toList())
        actualizarTotal()
    }

    /**
     * Elimina item del carrito
     */
    private fun eliminarDelCarrito(item: ItemPedido) {
        carrito.remove(item)
        actualizarCarrito(null)
        Toast.makeText(context, "✅ ${item.nombre} removido", Toast.LENGTH_SHORT).show()
    }

    /**
     * Actualiza total visible
     */
    private fun actualizarTotal() {
        val total = carrito.sumOf { it.cantidad * it.precioUnitario }
        textTotal.text = "Total: €${String.format("%.2f", total)}"
    }

    /**
     * Crea el pedido en Firebase
     */
    private fun crearPedido() {
        val huesped = huespedSeleccionado ?: return

        viewModel.crearPedido(huesped.id, carrito.toList())
            .observe(viewLifecycleOwner) { resultado ->
                when (resultado) {
                    "exito" -> {
                        Toast.makeText(context, "✅ Pedido creado exitosamente", Toast.LENGTH_SHORT).show()

                        // Limpia carrito
                        carrito.clear()
                        actualizarCarrito(null)

                        // Vuelve a dashboard
                        findNavController().navigate(R.id.action_pedidos_to_dashboard)
                    }
                    else -> {
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}