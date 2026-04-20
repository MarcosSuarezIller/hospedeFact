package com.example.hospedefact.ui.pedidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Habitacion
import com.example.hospedefact.data.models.Huesped
import com.example.hospedefact.data.models.ItemPedido
import com.example.hospedefact.data.models.MenuItem
import com.example.hospedefact.data.models.Mesa
import com.example.hospedefact.data.models.ProductoAlmacen
import com.example.hospedefact.ui.almacen.ProductoAlmacenViewModel
import com.example.hospedefact.ui.habitaciones.HabitacionViewModel
import com.example.hospedefact.ui.huespedes.HuespedViewModel
import com.example.hospedefact.ui.restauracion.MesaViewModel

/**
 * Fragmento encargado de la interfaz de creación de pedidos.
 * Proporciona una experiencia de usuario completa para gestionar el carrito de compras,
 * seleccionar el destinatario (Huésped o Mesa) y realizar la integración directa con
 * el sistema de inventario del almacén.
 */
class CrearPedidoFragment : Fragment() {

    private lateinit var viewModel: PedidoViewModel
    private lateinit var huespedViewModel: HuespedViewModel
    private lateinit var spinnerHuesped: Spinner
    private lateinit var spinnerMesa: Spinner
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var recyclerCarrito: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var btnCrearPedido: Button
    private lateinit var textTotal: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var carritoAdapter: CarritoAdapter
    private lateinit var containerMesa: LinearLayout
    private lateinit var containerHuesped: LinearLayout
    private lateinit var spinnerTipoPedido: Spinner
    private var huespedes = mutableListOf<Huesped>()
    private var huespedSeleccionado: Huesped? = null
    private var carrito = mutableListOf<ItemPedido>()

    private var mesasDisponibles = mutableListOf<Mesa>()

    private var tiposDisponibles = listOf("Huesped", "Mesa")

    private var mesaSeleccionada: Mesa? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_pedido, container, false)
    }

    /**
     * Inicializa los componentes de la interfaz, configura los adaptadores de las listas
     * y establece los observadores para la carga dinámica de datos.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = PedidoViewModel()
        huespedViewModel = HuespedViewModel()

        // Obtiene referencias
        spinnerHuesped = view.findViewById(R.id.spinner_huesped)
        spinnerMesa = view.findViewById(R.id.spinner_mesa)
        recyclerMenu = view.findViewById(R.id.recycler_menu)
        recyclerCarrito = view.findViewById(R.id.recycler_carrito)
        btnAtras = view.findViewById(R.id.btn_atras_pedido)
        btnCrearPedido = view.findViewById(R.id.btn_crear_pedido)
        textTotal = view.findViewById(R.id.text_total)
        progressBar = view.findViewById(R.id.progress_bar_pedido)

        spinnerTipoPedido = view.findViewById(R.id.spinner_tipo_pedido)
        containerHuesped = view.findViewById(R.id.container_huesped)
        containerMesa = view.findViewById(R.id.container_mesa)

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

        cargarMesasDisponibles()

        // Carga menú
        cargarMenu()

        // Configurar spinner de tipo
        val adapterTipo = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tiposDisponibles
        )
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoPedido.adapter = adapterTipo

        spinnerTipoPedido.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        containerHuesped.visibility = View.VISIBLE
                        containerMesa.visibility = View.GONE
                        cargarHuespedes()
                    }
                    1 -> {
                        containerHuesped.visibility = View.GONE
                        containerMesa.visibility = View.VISIBLE
                        cargarMesasDisponibles()
                    }
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    /**
     * Solicita la lista de huéspedes activos desde el ViewModel y configura el spinner
     * para permitir la asignación de pedidos a una estancia específica.
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
     * Despliega un diálogo emergente para registrar un nuevo huésped de forma rápida,
     * permitiendo asignar una habitación disponible antes de proceder con el pedido.
     */
    private fun mostrarDialogoNuevoHuesped() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_nuevo_huesped, null)

        val inputNombre = view.findViewById<EditText>(R.id.input_nombre_huesped)
        val spinnerHabitacion = view.findViewById<Spinner>(R.id.spinner_habitacion)
        val btnCrear = view.findViewById<Button>(R.id.btn_crear_huesped_dialog)

        val habitacionViewModel = HabitacionViewModel()
        var habitacionSeleccionada: Habitacion? = null

        habitacionViewModel.cargarHabitaciones().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val habitaciones = (resultado as? List<Habitacion>)?.toList() ?: emptyList()

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        habitaciones.map { "Hab ${it.numero} - ${it.tipo} (€${it.precioNoche})" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerHabitacion.adapter = adapter

                    spinnerHabitacion.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            habitacionSeleccionada = habitaciones.getOrNull(position)
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                }
            }
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        btnCrear.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val habitacion = habitacionSeleccionada

            if (nombre.isEmpty()) {
                Toast.makeText(context, "Ingresa nombre del huesped", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (habitacion == null) {
                Toast.makeText(context, "Selecciona una habitacion", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear huesped con habitacion seleccionada
            val huesped = Huesped(
                nombre = nombre,
                habitacion = "Hab ${habitacion.numero}",
                habitacionId = habitacion.id,
                precioNocheHabitacion = habitacion.precioNoche,
                estado = "activo",
                fechaEntrada = System.currentTimeMillis()
            )

            // Llamar a crear huesped del ViewModel
            huespedViewModel.crearHuesped(huesped).observe(viewLifecycleOwner) { resultado ->
                if (resultado == "exito") {
                    Toast.makeText(context, "Huesped creado", Toast.LENGTH_SHORT).show()
                    cargarHuespedes()
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Error: $resultado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    /**
     * Recupera las mesas que no están actualmente ocupadas para permitir que se realicen
     * pedidos directamente a través del servicio de restauración.
     */
    private fun cargarMesasDisponibles() {
        val mesaViewModel = MesaViewModel()

        mesaViewModel.cargarMesasDisponibles().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is String -> {
                    if (resultado.startsWith("error")) {
                        Toast.makeText(context, resultado, Toast.LENGTH_SHORT).show()
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    mesasDisponibles = (resultado as? List<Mesa>)?.toMutableList() ?: mutableListOf()

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        mesasDisponibles.map { "Mesa ${it.numero} - Cap: ${it.capacidad}" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerMesa.adapter = adapter

                    spinnerMesa.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            mesaSeleccionada = mesasDisponibles.getOrNull(position)
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            mesaSeleccionada = null
                        }
                    }
                }
            }
        }
    }

    /**
     * Obtiene los productos disponibles en el almacén y los transforma en artículos del menú.
     * Esto asegura que solo se puedan pedir productos que tengan stock físico en el inventario.
     */
    private fun cargarMenu() {
        val productoViewModel = ProductoAlmacenViewModel()

        productoViewModel.cargarProductos().observe(viewLifecycleOwner) { resultado ->
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
                    val productosAlmacen = resultado as? List<ProductoAlmacen> ?: emptyList()

                    // Convierte productos de almacén a MenuItem
                    val items = productosAlmacen.map { producto ->
                        MenuItem(
                            id = producto.id,
                            nombre = producto.nombre,
                            descripcion = "${producto.descripcion} (Stock: ${producto.stockActual})",
                            precio = producto.precioVenta,
                            categoria = "almacen",
                            activo = producto.stockActual > 0  // Solo activos si hay stock
                        )
                    }

                    menuAdapter.submitList(items)
                }
            }
        }
    }

    /**
     * Añade un producto seleccionado al carrito local. Si el producto ya existe,
     * incrementa su cantidad automáticamente.
     * 
     * @param item El artículo del menú a añadir.
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
     * Refresca la visualización de la lista del carrito y recalcula el importe total.
     * 
     * @param itemActualizado Opcional: el ítem que ha sufrido modificaciones en su cantidad.
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
     * Elimina permanentemente un artículo del carrito de compras actual.
     * 
     * @param item El artículo a eliminar.
     */
    private fun eliminarDelCarrito(item: ItemPedido) {
        carrito.remove(item)
        actualizarCarrito(null)
        Toast.makeText(context, "✅ ${item.nombre} removido", Toast.LENGTH_SHORT).show()
    }

    /**
     * Suma los importes de todos los elementos del carrito y actualiza la etiqueta de total.
     */
    private fun actualizarTotal() {
        val total = carrito.sumOf { it.cantidad * it.precioUnitario }
        textTotal.text = "Total: €${String.format("%.2f", total)}"
    }

    /**
     * Procesa la confirmación del pedido, validando el tipo de destinatario (Huésped o Mesa).
     * Invoca el método del ViewModel que descuenta automáticamente el stock del almacén.
     */
    private fun crearPedido() {
        val tipoPedido = spinnerTipoPedido.selectedItemPosition

        if (carrito.isEmpty()) {
            Toast.makeText(context, "Agrega productos al carrito", Toast.LENGTH_SHORT).show()
            return
        }

        when (tipoPedido) {
            0 -> {
                // Pedido de Huesped
                val huesped = huespedSeleccionado ?: return
                viewModel.crearPedidoConStockDescontado(huesped.id, carrito.toList())
                    .observe(viewLifecycleOwner) { resultado ->
                        when (resultado) {
                            "exito" -> {
                                Toast.makeText(context, "Pedido de huesped creado", Toast.LENGTH_SHORT).show()
                                carrito.clear()
                                actualizarCarrito(null)
                                findNavController().navigate(R.id.action_pedidos_to_dashboard)
                            }
                            else -> {
                                Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
            1 -> {
                // Pedido de Mesa
                val mesa = mesaSeleccionada ?: return
                crearPedidoMesa(mesa)
            }
        }
    }

    /**
     * Gestiona la creación de un pedido asignado a una mesa física y actualiza el estado
     * de la mesa a 'ocupada' tras una confirmación exitosa.
     * 
     * @param mesa El objeto mesa donde se está realizando el servicio.
     */
    private fun crearPedidoMesa(mesa: Mesa) {
        viewModel.crearPedidoConStockDescontado(mesa.id, carrito.toList())
            .observe(viewLifecycleOwner) { resultado ->
                when (resultado) {
                    "exito" -> {
                        Toast.makeText(context, "Pedido de mesa creado", Toast.LENGTH_SHORT).show()

                        val mesaViewModel = MesaViewModel()
                        mesaViewModel.ocuparMesa(mesa.id, "cliente_restaurante", "").observe(viewLifecycleOwner) { res ->
                            if (res == "exito") {
                                carrito.clear()
                                actualizarCarrito(null)
                                findNavController().navigate(R.id.action_pedidos_to_dashboard)
                            }
                        }
                    }
                    else -> {
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}