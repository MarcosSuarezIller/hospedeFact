package com.example.hospedefact.ui.almacen

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.ItemOrdenCompra
import com.example.hospedefact.data.models.Proveedor
import com.example.hospedefact.data.models.ProductoAlmacen
import java.util.Calendar

/**
 * CrearOrdenCompraFragment
 * Formulario para crear órdenes de compra
 */
class CrearOrdenCompraFragment : Fragment() {

    private lateinit var proveedorViewModel: ProveedorViewModel
    private lateinit var productoViewModel: ProductoAlmacenViewModel
    private lateinit var ordenViewModel: OrdenCompraViewModel

    // Vistas
    private lateinit var spinnerProveedor: Spinner
    private lateinit var spinnerProducto: Spinner
    private lateinit var inputCantidad: EditText
    private lateinit var inputNotas: EditText
    private lateinit var recyclerItems: RecyclerView
    private lateinit var textTotal: TextView
    private lateinit var btnAtras: Button
    private lateinit var btnAgregarItem: Button
    private lateinit var btnCrearOrden: Button
    private lateinit var progressBar: ProgressBar

    private var proveedores = mutableListOf<Proveedor>()
    private var productos = mutableListOf<ProductoAlmacen>()
    private var items = mutableListOf<ItemOrdenCompra>()
    private var proveedorSeleccionado: Proveedor? = null
    private var itemAdapter: ItemOrdenCompraAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crear_orden_compra, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        proveedorViewModel = ProveedorViewModel()
        productoViewModel = ProductoAlmacenViewModel()
        ordenViewModel = OrdenCompraViewModel()

        // Obtiene referencias
        spinnerProveedor = view.findViewById(R.id.spinner_proveedor_orden)
        spinnerProducto = view.findViewById(R.id.spinner_producto_orden)
        inputCantidad = view.findViewById(R.id.input_cantidad_orden)
        inputNotas = view.findViewById(R.id.input_notas_orden)
        recyclerItems = view.findViewById(R.id.recycler_items_orden)
        textTotal = view.findViewById(R.id.text_total_orden_crear)
        btnAtras = view.findViewById(R.id.btn_atras_crear_orden)
        btnAgregarItem = view.findViewById(R.id.btn_agregar_item)
        btnCrearOrden = view.findViewById(R.id.btn_crear_orden)
        progressBar = view.findViewById(R.id.progress_bar_crear_orden)

        // Configura RecyclerView
        itemAdapter = ItemOrdenCompraAdapter { item ->
            items.remove(item)
            itemAdapter?.submitList(items.toList())
            actualizarTotal()
        }
        recyclerItems.adapter = itemAdapter
        recyclerItems.layoutManager = LinearLayoutManager(context)

        // Botones
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_crear_orden_to_ordenes)
        }

        btnAgregarItem.setOnClickListener {
            agregarItem()
        }

        btnCrearOrden.setOnClickListener {
            crearOrden()
        }

        // Cargar datos
        cargarProveedores()
        cargarProductos()
    }

    /**
     * Carga proveedores
     */
    private fun cargarProveedores() {
        proveedorViewModel.cargarProveedores().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    proveedores = (resultado as? List<Proveedor>)?.toMutableList() ?: mutableListOf()

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        proveedores.map { it.nombre }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerProveedor.adapter = adapter

                    spinnerProveedor.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            proveedorSeleccionado = proveedores.getOrNull(position)
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            proveedorSeleccionado = null
                        }
                    }
                }
            }
        }
    }

    /**
     * Carga productos disponibles
     */
    private fun cargarProductos() {
        productoViewModel.cargarProductos().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    productos = (resultado as? List<ProductoAlmacen>)?.toMutableList() ?: mutableListOf()

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        productos.map { it.nombre }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerProducto.adapter = adapter
                }
            }
        }
    }

    /**
     * Agrega item a la orden
     */
    private fun agregarItem() {
        val producto = productos.getOrNull(spinnerProducto.selectedItemPosition)
        val cantidad = inputCantidad.text.toString().toIntOrNull() ?: 0

        if (producto == null) {
            Toast.makeText(context, "⚠️ Selecciona un producto", Toast.LENGTH_SHORT).show()
            return
        }

        if (cantidad <= 0) {
            Toast.makeText(context, "⚠️ Cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear item
        val item = ItemOrdenCompra(
            productoId = producto.id,
            productoNombre = producto.nombre,
            cantidad = cantidad,
            unidad = producto.unidad,
            precioUnitario = producto.precioCompra,
            subtotal = cantidad * producto.precioCompra,
            cantidadRecibida = cantidad
        )

        items.add(item)
        itemAdapter?.submitList(items.toList())

        // Limpiar
        inputCantidad.text.clear()
        spinnerProducto.setSelection(0)

        actualizarTotal()
        Toast.makeText(context, "✅ Item agregado", Toast.LENGTH_SHORT).show()
    }

    /**
     * Actualiza el total
     */
    private fun actualizarTotal() {
        val subtotal = items.sumOf { it.subtotal }
        val impuestos = subtotal * 0.21
        val total = subtotal + impuestos

        textTotal.text = "Subtotal: €${String.format("%.2f", subtotal)}\n" +
                "IVA (21%): €${String.format("%.2f", impuestos)}\n" +
                "Total: €${String.format("%.2f", total)}"
    }

    /**
     * Crea la orden de compra
     */
    private fun crearOrden() {
        val proveedor = proveedorSeleccionado

        if (proveedor == null) {
            Toast.makeText(context, "⚠️ Selecciona un proveedor", Toast.LENGTH_SHORT).show()
            return
        }

        if (items.isEmpty()) {
            Toast.makeText(context, "⚠️ Agrega al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        val subtotal = items.sumOf { it.subtotal }
        val impuestos = subtotal * 0.21
        val total = subtotal + impuestos

        // Calcular fecha de entrega esperada
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, proveedor.tiempoEntrega)
        val fechaEntregaEsperada = calendar.timeInMillis

        // Crear orden
        ordenViewModel.crearOrdenCompra(
            proveedorId = proveedor.id,
            proveedorNombre = proveedor.nombre,
            items = items,
            subtotal = subtotal,
            impuestos = impuestos,
            total = total,
            fechaEntregaEsperada = fechaEntregaEsperada,
            notas = inputNotas.text.toString().trim()
        ).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    btnCrearOrden.isEnabled = false
                }
                "exito" -> {
                    progressBar.visibility = View.GONE
                    btnCrearOrden.isEnabled = true
                    Toast.makeText(context, "✅ Orden creada exitosamente", Toast.LENGTH_SHORT).show()

                    // Vuelve a la lista
                    findNavController().navigate(R.id.action_crear_orden_to_ordenes)
                }
                else -> {
                    progressBar.visibility = View.GONE
                    btnCrearOrden.isEnabled = true
                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}