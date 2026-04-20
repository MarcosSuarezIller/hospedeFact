package com.example.hospedefact.ui.restauracion

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
import com.example.hospedefact.data.models.Mesa

/**
 * Fragmento encargado de la administración y configuración de las mesas del restaurante.
 * Permite al administrador visualizar el listado completo, añadir nuevas mesas con su
 * respectiva capacidad y ubicación, así como eliminar mesas existentes.
 */
class AdminMesasFragment : Fragment() {

    private lateinit var viewModel: MesaViewModel
    private lateinit var adapter: AdminMesasAdapter

    private lateinit var recyclerMesas: RecyclerView
    private lateinit var btnAtras: Button
    private lateinit var inputNumeroMesa: EditText
    private lateinit var inputCapacidad: EditText
    private lateinit var inputUbicacion: EditText
    private lateinit var btnAgregarMesa: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textVacio: TextView
    private lateinit var textTotal: TextView

    /**
     * Infla el diseño XML correspondiente a la administración de mesas.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_mesas, container, false)
    }

    /**
     * Inicializa las referencias a los componentes de la interfaz, configura el adaptador
     * del RecyclerView y establece los listeners para los botones de acción.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = MesaViewModel()

        recyclerMesas = view.findViewById(R.id.recycler_admin_mesas)
        btnAtras = view.findViewById(R.id.btn_atras_admin_mesas)
        inputNumeroMesa = view.findViewById(R.id.input_numero_mesa)
        inputCapacidad = view.findViewById(R.id.input_capacidad_mesa)
        inputUbicacion = view.findViewById(R.id.input_ubicacion_mesa)
        btnAgregarMesa = view.findViewById(R.id.btn_agregar_mesa)
        progressBar = view.findViewById(R.id.progress_bar_admin_mesas)
        textVacio = view.findViewById(R.id.text_vacio_admin_mesas)
        textTotal = view.findViewById(R.id.text_total_mesas)

        adapter = AdminMesasAdapter { mesa ->
            eliminarMesa(mesa)
        }

        recyclerMesas.adapter = adapter
        recyclerMesas.layoutManager = LinearLayoutManager(context)

        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_admin_mesas_to_dashboard)
        }

        btnAgregarMesa.setOnClickListener {
            agregarMesa()
        }

        cargarMesas()
    }

    /**
     * Recupera el listado completo de mesas desde el ViewModel, gestionando la visualización
     * de estados de carga, lista vacía y errores de red o base de datos.
     */
    private fun cargarMesas() {
        viewModel.cargarMesas().observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerMesas.visibility = View.GONE
                    textVacio.visibility = View.GONE
                }
                is String -> {
                    if (resultado.startsWith("error")) {
                        progressBar.visibility = View.GONE
                        recyclerMesas.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = resultado
                    }
                }
                is List<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val mesas = resultado as? List<Mesa> ?: emptyList()

                    progressBar.visibility = View.GONE

                    if (mesas.isEmpty()) {
                        recyclerMesas.visibility = View.GONE
                        textVacio.visibility = View.VISIBLE
                        textVacio.text = "No hay mesas. Agrega una nueva."
                        textTotal.text = "Total: 0 mesas"
                    } else {
                        recyclerMesas.visibility = View.VISIBLE
                        textVacio.visibility = View.GONE
                        adapter.submitList(mesas.sortedBy { it.numero })
                        textTotal.text = "Total: ${mesas.size} mesas"
                    }
                }
            }
        }
    }

    /**
     * Valida los datos introducidos en los campos de texto y solicita la creación
     * de una nueva mesa a través del ViewModel.
     */
    private fun agregarMesa() {
        val numero = inputNumeroMesa.text.toString().toIntOrNull()
        val capacidad = inputCapacidad.text.toString().toIntOrNull() ?: 4
        val ubicacion = inputUbicacion.text.toString().trim()

        if (numero == null || numero <= 0) {
            Toast.makeText(context, "Ingresa un numero de mesa valido", Toast.LENGTH_SHORT).show()
            return
        }

        if (ubicacion.isEmpty()) {
            Toast.makeText(context, "Ingresa la ubicacion de la mesa", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.crearMesa(numero, capacidad, ubicacion).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "exito" -> {
                    Toast.makeText(context, "Mesa $numero agregada", Toast.LENGTH_SHORT).show()
                    inputNumeroMesa.text.clear()
                    inputCapacidad.text.clear()
                    inputUbicacion.text.clear()
                    cargarMesas()
                }
                else -> {
                    Toast.makeText(context, resultado, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de proceder con la eliminación
     * de una mesa seleccionada del sistema.
     * 
     * @param mesa El objeto mesa que se desea borrar.
     */
    private fun eliminarMesa(mesa: Mesa) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Mesa ${mesa.numero}")
            .setMessage("Confirma que deseas eliminar esta mesa")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarMesa(mesa.id).observe(viewLifecycleOwner) { resultado ->
                    when (resultado) {
                        "exito" -> {
                            Toast.makeText(context, "Mesa eliminada", Toast.LENGTH_SHORT).show()
                            cargarMesas()
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