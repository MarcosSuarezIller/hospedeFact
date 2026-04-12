package com.example.hospedefact.ui.almacen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hospedefact.R

/**
 * NuevoProveedorFragment
 * Formulario para crear nuevos proveedores
 */
class NuevoProveedorFragment : Fragment() {

    private lateinit var viewModel: ProveedorViewModel

    // Campos del formulario
    private lateinit var inputNombre: EditText
    private lateinit var inputContacto: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputTelefono: EditText
    private lateinit var inputDireccion: EditText
    private lateinit var inputCiudad: EditText
    private lateinit var inputPais: EditText
    private lateinit var inputCodigoPostal: EditText
    private lateinit var inputTiempoEntrega: EditText

    // Botones
    private lateinit var btnAtras: Button
    private lateinit var btnGuardar: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nuevo_proveedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ProveedorViewModel()

        // Obtiene referencias
        inputNombre = view.findViewById(R.id.input_proveedor_nombre)
        inputContacto = view.findViewById(R.id.input_contacto)
        inputEmail = view.findViewById(R.id.input_email_prov)
        inputTelefono = view.findViewById(R.id.input_telefono_prov)
        inputDireccion = view.findViewById(R.id.input_direccion)
        inputCiudad = view.findViewById(R.id.input_ciudad)
        inputPais = view.findViewById(R.id.input_pais)
        inputCodigoPostal = view.findViewById(R.id.input_codigo_postal)
        inputTiempoEntrega = view.findViewById(R.id.input_tiempo_entrega)

        // Botones
        btnAtras = view.findViewById(R.id.btn_atras_nuevo_proveedor)
        btnGuardar = view.findViewById(R.id.btn_guardar_proveedor)
        progressBar = view.findViewById(R.id.progress_bar_nuevo_proveedor)

        // Listeners
        btnAtras.setOnClickListener {
            findNavController().navigate(R.id.action_nuevo_proveedor_to_proveedores)
        }

        btnGuardar.setOnClickListener {
            guardarProveedor()
        }
    }

    /**
     * Guarda el nuevo proveedor
     */
    private fun guardarProveedor() {
        // Obtiene valores
        val nombre = inputNombre.text.toString().trim()
        val contacto = inputContacto.text.toString().trim()
        val email = inputEmail.text.toString().trim()
        val telefono = inputTelefono.text.toString().trim()
        val direccion = inputDireccion.text.toString().trim()
        val ciudad = inputCiudad.text.toString().trim()
        val pais = inputPais.text.toString().trim()
        val codigoPostal = inputCodigoPostal.text.toString().trim()
        val tiempoEntrega = inputTiempoEntrega.text.toString().toIntOrNull() ?: 3

        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(context, "⚠️ Nombre, email y teléfono son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "⚠️ Email inválido", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear proveedor
        viewModel.crearProveedor(
            nombre = nombre,
            contacto = contacto,
            email = email,
            telefono = telefono,
            direccion = direccion,
            ciudad = ciudad,
            pais = pais,
            codigoPostal = codigoPostal,
            tiempoEntrega = tiempoEntrega
        ).observe(viewLifecycleOwner) { resultado ->
            when (resultado) {
                "cargando" -> {
                    progressBar.visibility = View.VISIBLE
                    btnGuardar.isEnabled = false
                }
                "exito" -> {
                    progressBar.visibility = View.GONE
                    btnGuardar.isEnabled = true
                    Toast.makeText(context, "✅ Proveedor creado exitosamente", Toast.LENGTH_SHORT).show()

                    // Vuelve a la lista
                    findNavController().navigate(R.id.action_nuevo_proveedor_to_proveedores)
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