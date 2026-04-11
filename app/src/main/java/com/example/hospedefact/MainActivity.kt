package com.example.hospedefact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hospedefact.ui.auth.LoginFragment
import androidx.fragment.app.commit
import com.example.hospedefact.utils.InitialDataLoader

/**
 * MainActivity
 * Contenedor principal de la app
 */
class MainActivity : AppCompatActivity() {

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