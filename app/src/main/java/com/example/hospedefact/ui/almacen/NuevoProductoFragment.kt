package com.example.hospedefact.ui.almacen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hospedefact.R
import com.example.hospedefact.data.models.ProductoAlmacen
import com.example.hospedefact.data.repository.ProveedorRepository

/**
 * NuevoProductoFragment
 * Formulario para crear nuevos productos en el almacén
 */
class NuevoProductoFragment : Fragment() {

    private lateinit var viewModel: ProductoAlmacenViewModel
    private lateinit var proveedorViewModel: ProveedorViewModel

    // Campos del formulario
    private lateinit var inputNombre: EditText
    private lateinit var inputDescripcion: EditText
    private lateinit var spinnerProveedor: Spinner
    private lateinit var inputStockMinimo: EditText
    private lateinit var inputStockMaximo: EditText
    private lateinit var inputUbicacion: EditText
    private lateinit var inputPrecioCompra: EditText
    private lateinit var inputPrecioVenta: EditText

    // Botones
    private lateinit var btnAtras: Button
    private lateinit var btnGuardar: Button
    private lateinit var progressBar: ProgressBar

    private var proveedores = mutableListOf<String>()
    private var proveedorSeleccionado = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nuevo_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProductoAlmacenViewModel()
        proveedorViewModel = ProveedorViewModel()

        // Obtiene referencias de EditText
        inputNombre = view.findViewById(R.id.input_producto_nombre)
        inputDescripcion = view.findViewById(R.id.input_descripcion)
        spinnerProveedor = view.findViewById(R.id.spinner_proveedor_producto)
        inputStockMinimo = view.findViewById(R.id.input_stock_minimo)
        inputStockMaximo = view.findViewById(R.id.input_stock_maximo)
        inputUbicacion = view.findViewById(R.id.input_ubicacion)
        inputPrecioCompra = view.findViewById(R.id.input_precio_compra)
        inputPrecioVenta = view.findViewById(R.id.input_precio_venta)

        // Botones
        btnAtras = view.findViewById(R.id.btn_atras_nuevo_producto)
        btnGuardar = view.findViewById(R.id.btn_guardar_producto)
        progressBar = view.findViewById(R.id.progress_bar_nuevo_producto)

        // Listeners
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_nuevo_producto_to_productos)
        }

        btnGuardar.setOnClickListener {
            guardarProducto()
        }

        // Cargar proveedores
        cargarProveedores()
    }

    /**
     * Carga lista de proveedores para el spinner
     */
    private fun cargarProveedores() {
        proveedorViewModel.cargarProveedores().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val provs = (resultado as? List<*>)?.filterIsInstance<com.example.hospedefact.data.models.Proveedor>() ?: emptyList()

                    proveedores.clear()
                    proveedores.addAll(provs.map { it.nombre })

                    // Crear adaptador para spinner
                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        proveedores
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerProveedor.adapter = adapter

                    spinnerProveedor.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            proveedorSeleccionado = if (position >= 0 && position < proveedores.size) {
                                proveedores[position]
                            } else {
                                ""
                            }
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            proveedorSeleccionado = ""
                        }
                    }
                }
            }
        }
    }

    /**
     * Guarda el nuevo producto
     */
    private fun guardarProducto() {
        // Obtiene valores
        val nombre = inputNombre.text.toString().trim()
        val descripcion = inputDescripcion.text.toString().trim()
        val stockMinimo = inputStockMinimo.text.toString().toIntOrNull() ?: 5
        val stockMaximo = inputStockMaximo.text.toString().toIntOrNull() ?: 100
        val ubicacion = inputUbicacion.text.toString().trim()
        val precioCompra = inputPrecioCompra.text.toString().toDoubleOrNull() ?: 0.0
        val precioVenta = inputPrecioVenta.text.toString().toDoubleOrNull() ?: 0.0

        // Validaciones
        if (nombre.isEmpty()) {
            Toast.makeText(context, "⚠️ El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        if (precioCompra <= 0.0 || precioVenta <= 0.0) {
            Toast.makeText(context, "⚠️ Los precios deben ser mayores a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (precioVenta <= precioCompra) {
            Toast.makeText(context, "⚠️ El precio de venta debe ser mayor al de compra", Toast.LENGTH_SHORT).show()
            return
        }

        if (proveedorSeleccionado.isEmpty()) {
            Toast.makeText(context, "⚠️ Selecciona un proveedor", Toast.LENGTH_SHORT).show()
            return
        }

        if (stockMinimo >= stockMaximo) {
            Toast.makeText(context, "⚠️ Stock mínimo debe ser menor al máximo", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear producto
        viewModel.crearProductoAlmacen(
            nombre = nombre,
            descripcion = descripcion,
            proveedor = proveedorSeleccionado,
            stockMinimo = stockMinimo,
            stockMaximo = stockMaximo,
            unidad = "unidades",
            precioCompra = precioCompra,
            precioVenta = precioVenta,
            ubicacion = ubicacion
        ).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    btnGuardar.isEnabled = false
                }
                "exito" -> {
                    progressBar.visibility = View.GONE
                    btnGuardar.isEnabled = true
                    Toast.makeText(context, "✅ Producto creado exitosamente", Toast.LENGTH_SHORT).show()

                    // Vuelve a la lista
                    findNavController().navigate(R.id.action_nuevo_producto_to_productos)
                }
                else -> {
                    progressBar.visibility = View.GONE
                    btnGuardar.isEnabled = true
                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}