package com.example.hospedefact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hospedefact.ui.auth.LoginFragment
import androidx.fragment.app.commit
import com.example.hospedefact.utils.InitialDataLoader

/**
 * Actividad principal del proyecto HospedeFact.
 * Actúa como el contenedor base para el sistema de navegación por fragmentos
 * y es el punto de entrada de la aplicación tras su lanzamiento.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Inicializa la actividad y configura la vista principal.
     * En este punto se puede orquestar la carga de datos iniciales o la navegación
     * hacia el fragmento de inicio según el estado de la sesión del usuario.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establece el layout principal (sin XML, solo contenedor de fragmentos)
        setContentView(R.layout.activity_main)

        //Modificable en el futuro para una carga inicial
        //InitialDataLoader.cargarMenuInicial()

        // Si es la primera vez, muestra LoginFragment
//        if (savedInstanceState == null) {
//            supportFragmentManager.commit {
//                replace(R.id.fragment_container, LoginFragment())
//            }
//        }
    }
}