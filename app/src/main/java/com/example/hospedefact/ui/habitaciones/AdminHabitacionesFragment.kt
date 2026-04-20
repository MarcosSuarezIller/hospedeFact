package com.example.hospedefact.ui.habitaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Habitacion

/**
 * Fragmento encargado de la gestión administrativa de las habitaciones del hotel.
 * Permite listar todas las habitaciones, registrar nuevas estancias con sus especificaciones
 * (número, tipo, precio, capacidad) y eliminar habitaciones existentes del sistema.
 */
class AdminHabitacionesFragment : Fragment() {

    private lateinit var viewModel: HabitacionViewModel
    private lateinit var adapter: AdminHabitacionesAdapter

    private lateinit var recyclerHabitaciones: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var inputNumero: EditText
    private lateinit var inputTipo: EditText
    private lateinit var inputPrecio: EditText
    private lateinit var inputCapacidad: EditText
    private lateinit var btnAgregarHabitacion: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView
    private lateinit var textTotal: TextView

    /**
     * Infla la vista del fragmento desde el recurso de diseño XML.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_habitaciones, container, false)
    }

    /**
     * Inicializa los componentes de la interfaz, configura el adaptador para el listado de
     * habitaciones y define el comportamiento de los botones de navegación y acción.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = HabitacionViewModel()

        recyclerHabitaciones = view.findViewById(R.id.recycler_admin_habitaciones)
        btnAtras = view.findViewById(R.id.btn_atras_admin_habitaciones)
        inputNumero = view.findViewById(R.id.input_numero_habitacion)
        inputTipo = view.findViewById(R.id.input_tipo_habitacion)
        inputPrecio = view.findViewById(R.id.input_precio_habitacion)
        inputCapacidad = view.findViewById(R.id.input_capacidad_habitacion)
        btnAgregarHabitacion = view.findViewById(R.id.btn_agregar_habitacion)
        progressBar = view.findViewById(R.id.progress_bar_admin_habitaciones)
        textVacio = view.findViewById(R.id.text_vacio_admin_habitaciones)
        textTotal = view.findViewById(R.id.text_total_habitaciones)

        adapter = AdminHabitacionesAdapter { habitacion ->
            eliminarHabitacion(habitacion)
        }

        recyclerHabitaciones.adapter = adapter
        recyclerHabitaciones.layoutManager = LinearLayoutManager(context)

        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_admin_habitaciones_to_dashboard)
        }

        btnAgregarHabitacion.setOnClickListener {
            agregarHabitacion()
        }

        cargarHabitaciones()
    }

    /**
     * Solicita la carga de todas las habitaciones al [HabitacionViewModel] y gestiona
     * la visibilidad de la barra de progreso y el estado de la lista.
     */
    private fun cargarHabitaciones() {
        viewModel.cargarHabitaciones().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerHabitaciones.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerHabitaciones.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val habitaciones = resultado as? List<Habitacion> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (habitaciones.isEmpty()) {
                        recyclerHabitaciones.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay habitaciones. Agrega una nueva."
                        textTotal.text = "Total: 0 habitaciones"
                    } else {
                        recyclerHabitaciones.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(habitaciones.sortedBy { it.numero })
                        textTotal.text = "Total: ${habitaciones.size} habitaciones"
                    }
                }
            }
        }
    }

    /**
     * Recopila y valida la información de los campos de entrada para registrar una nueva
     * habitación a través del repositorio de datos.
     */
    private fun agregarHabitacion() {
        val numero = inputNumero.text.toString().toIntOrNull()
        val tipo = inputTipo.text.toString().trim()
        val precio = inputPrecio.text.toString().toDoubleOrNull()
        val capacidad = inputCapacidad.text.toString().toIntOrNull() ?: 2

        if (numero == null || numero <= 0) {
            Toast.makeText(context, "Ingresa un numero de habitacion valido", Toast.LENGTH_SHORT).show()
            return
        }

        if (tipo.isEmpty()) {
            Toast.makeText(context, "Ingresa el tipo de habitacion", Toast.LENGTH_SHORT).show()
            return
        }

        if (precio == null || precio <= 0) {
            Toast.makeText(context, "Ingresa un precio valido", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.crearHabitacion(numero, tipo, precio, capacidad).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "exito" -> {
                    Toast.makeText(context, "Habitacion $numero agregada", Toast.LENGTH_SHORT).show()
                    inputNumero.text.clear()
                    inputTipo.text.clear()
                    inputPrecio.text.clear()
                    inputCapacidad.text.clear()
                    cargarHabitaciones()
                }
                else -> {
                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación para eliminar una habitación seleccionada.
     * 
     * @param habitacion Objeto [Habitacion] que se desea eliminar.
     */
    private fun eliminarHabitacion(habitacion: Habitacion) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Habitacion ${habitacion.numero}")
            .setMessage("Confirma que deseas eliminar esta habitacion")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarHabitacion(habitacion.id).observe(viewLifecycleOwner) { resultado ->
                    when (resultado) {
                        "exito" -> {
                            Toast.makeText(context, "Habitacion eliminada", Toast.LENGTH_SHORT).show()
                            cargarHabitaciones()
                        }
                        else -> {
                            Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}