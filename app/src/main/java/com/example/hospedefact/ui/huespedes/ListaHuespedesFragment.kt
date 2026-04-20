package com.example.hospedefact.ui.huespedes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hospedefact.R
import com.example.hospedefact.data.models.Huesped
import android.app.AlertDialog
import android.widget.EditText
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.example.hospedefact.data.models.Habitacion
import com.example.hospedefact.ui.habitaciones.HabitacionViewModel

/**
 * ListaHuespedesFragment
 * Muestra lista de huéspedes activos
 * Permite crear nuevo huésped
 */
class ListaHuespedesFragment : Fragment() {

    private lateinit var viewModel: HuespedViewModel
    private lateinit var adapter: HuespedAdapter

    // Vistas
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnNuevoHuesped: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_huespedes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa ViewModel
        viewModel = HuespedViewModel()

        // Obtiene referencias
        recyclerView = view.findViewById(R.id.recycler_huespedes)
        btnNuevoHuesped = view.findViewById(R.id.btn_nuevo_huesped)
        progressBar = view.findViewById(R.id.progress_bar)
        textVacio = view.findViewById(R.id.text_vacio)

        // Configura RecyclerView
        adapter = HuespedAdapter { huesped ->
            // Click en huésped: podría abrir detalles
            Toast.makeText(context, "Huésped: ${huesped.nombre}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Botón para crear nuevo huésped
        btnNuevoHuesped.setOnClickListener {
            mostrarDialogoNuevoHuesped()
        }

        // Botón atrás
        val btnAtras = view.findViewById<Button>(R.id.btn_atras)
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_huespedes_to_dashboard)
        }

        // Carga la lista
        cargarHuespedes()
    }

    /**
     * Carga la lista de huéspedes desde Firestore
     */
    private fun cargarHuespedes() {
        viewModel.cargarHuespedes().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado

                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val huespedes = resultado as? List<Huesped> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (huespedes.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay huéspedes activos"
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(huespedes)
                    }
                }
            }
        }
    }

    /**
     * Muestra diálogo para crear nuevo huésped
     */
    private fun mostrarDialogoNuevoHuesped() {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_nuevo_huesped, null)

        val inputNombre = view.findViewById<EditText>(R.id.input_nombre_huesped)
        val spinnerHabitacion = view.findViewById<Spinner>(R.id.spinner_habitacion)
        val btnCrear = view.findViewById<Button>(R.id.btn_crear_huesped_dialog)
        val btnCancelar = view.findViewById<Button>(R.id.btn_cancelar_huesped_dialog)

        val habitacionViewModel = HabitacionViewModel()
        var habitacionSeleccionada: Habitacion? = null

        habitacionViewModel.cargarHabitaciones().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val habitaciones = (resultado as? List<Habitacion>)?.toList() ?: emptyList()

                    if (habitaciones.isEmpty()) {
                        Toast.makeText(context, "No hay habitaciones disponibles", Toast.LENGTH_SHORT).show()
                        return@observe
                    }

                    val adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        habitaciones.map { "Hab ${it.numero} - ${it.tipo} (€${String.format("%.2f", it.precioNoche)})" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerHabitacion.adapter = adapter

                    spinnerHabitacion.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            habitacionSeleccionada = habitaciones.getOrNull(position)
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            habitacionSeleccionada = null
                        }
                    }
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        Toast.makeText(context, resultado, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnCrear.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val habitacion = habitacionSeleccionada

            if (nombre.isEmpty()) {
                Toast.makeText(context, "Ingresa el nombre del huesped", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (habitacion == null) {
                Toast.makeText(context, "Selecciona una habitacion", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val huesped = Huesped(
                nombre = nombre,
                habitacion = "Hab ${habitacion.numero}",
                habitacionId = habitacion.id,
                precioNocheHabitacion = habitacion.precioNoche,
                estado = "activo",
                fechaEntrada = System.currentTimeMillis()
            )

            val huespedViewModel = HuespedViewModel()
            huespedViewModel.crearHuesped(huesped).observe(viewLifecycleOwner) { resultado ->
                when (resultado) {
                    "exito" -> {
                        Toast.makeText(context, "Huesped creado", Toast.LENGTH_SHORT).show()
                        cargarHuespedes()
                        dialog.dismiss()
                    }
                    else -> {
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    /**
     * Crea nuevo huésped
     */
    private fun crearHuesped(
        nombre: String,
        email: String,
        telefono: String,
        habitacion: String
    ) {
        viewModel.crearHuesped(nombre, email, telefono, habitacion)
            .observe(viewLifecycleOwner) { resultado ->
                when (resultado) {
                    "exito" -> {
                        Toast.makeText(context, "✅ Huésped creado", Toast.LENGTH_SHORT).show()
                        // Recarga la lista
                        cargarHuespedes()
                    }
                    else -> {
                        Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
