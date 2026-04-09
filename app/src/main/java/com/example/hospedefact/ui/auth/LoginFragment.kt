package com.example.hospedefact.ui.auth



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hospedefact.R

/**
 * Fragment de Login
 * Permite al usuario iniciar sesión con email y contraseña
 */
class LoginFragment : Fragment() {

    // ViewModel para manejar autenticación
    private lateinit var viewModel: LoginViewModel

    // Vistas (componentes UI)
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var mensajeTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el layout
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa ViewModel
        viewModel = LoginViewModel()

        // Obtiene referencias a las vistas
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        loginButton = view.findViewById(R.id.login_button)
        mensajeTextView = view.findViewById(R.id.mensaje)
        progressBar = view.findViewById(R.id.progress_bar)

        // Listener del botón de login
        loginButton.setOnClickListener {
            // Obtiene email y contraseña
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            // Valida que no estén vacíos
            if (email.isEmpty() || password.isEmpty()) {
                mensajeTextView.text = "⚠️ Completa todos los campos"
                mensajeTextView.setTextColor(android.graphics.Color.RED)
                return@setOnClickListener
            }

            // Valida que email sea válido
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                mensajeTextView.text = "⚠️ Email inválido"
                mensajeTextView.setTextColor(android.graphics.Color.RED)
                return@setOnClickListener
            }

            // Llama al ViewModel para hacer login
            viewModel.login(email, password).observe(viewLifecycleOwner) { resultado ->
                when (resultado) {
                    "cargando" -> {
                        progressBar.visibility = View.VISIBLE
                        loginButton.isEnabled = false
                        mensajeTextView.text = "Iniciando sesión..."
                        mensajeTextView.setTextColor(android.graphics.Color.GRAY)
                    }
                    "exito" -> {
                        progressBar.visibility = View.GONE
                        loginButton.isEnabled = true
                        mensajeTextView.text = "✅ ¡Bienvenido!"
                        mensajeTextView.setTextColor(android.graphics.Color.GREEN)

                        // TODO: Navegar a Dashboard
                        view.postDelayed({
                            findNavController().navigate(R.id.action_login_to_dashboard)
                        }, 1000)
                        //findNavController().navigate(R.id.action_login_to_dashboard)
                    }
                    else -> {
                        progressBar.visibility = View.GONE
                        loginButton.isEnabled = true
                        mensajeTextView.text = resultado
                        mensajeTextView.setTextColor(android.graphics.Color.RED)
                    }
                }
            }
        }
    }
}
