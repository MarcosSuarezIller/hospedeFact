package com.example.hospedefact.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hospedefact.R
import com.google.firebase.auth.FirebaseAuth

/**
 * DashboardFragment
 * Pantalla principal con menú de opciones
 */
class DashboardFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth

    // Botones del menú
    private lateinit var btnHuespedes: Button
    private lateinit var btnPedidos: Button
    private lateinit var btnFacturas: Button
    private lateinit var btnAlmacen: Button
    private lateinit var btnLogout: Button

    // Texto de bienvenida
    private lateinit var textoBienvenida: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Obtiene referencias a los botones
        btnHuespedes = view.findViewById(R.id.btn_huespedes)
        btnPedidos = view.findViewById(R.id.btn_pedidos)
        btnFacturas = view.findViewById(R.id.btn_facturas)
        btnAlmacen = view.findViewById(R.id.btn_almacen)
        btnLogout = view.findViewById(R.id.btn_logout)
        textoBienvenida = view.findViewById(R.id.texto_bienvenida)

        // Muestra email del usuario actual
        val emailUsuario = firebaseAuth.currentUser?.email ?: "Usuario"
        textoBienvenida.text = "Bienvenido, $emailUsuario"

        // Listeners de botones
        btnHuespedes.setOnClickListener {
             findNavController().navigate(R.id.action_dashboard_to_huespedes)
        }

        btnPedidos.setOnClickListener {
             findNavController().navigate(R.id.action_dashboard_to_pedidos)
        }

        btnFacturas.setOnClickListener {
             findNavController().navigate(R.id.action_dashboard_to_factura)
        }

        // Botón Almacén
        btnAlmacen.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_almacen)
        }

        btnLogout.setOnClickListener {
            // Cierra sesión
            firebaseAuth.signOut()

            // Navega de vuelta a LoginFragment
            // TODO: Implementar navegación
            findNavController().navigate(R.id.action_dashboard_to_login)
        }
    }
}
