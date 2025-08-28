package tech.blackdeath.contanexo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import tech.blackdeath.contanexo.navegacion.AppNavHost
import tech.blackdeath.contanexo.interfaz.theme.ContaNexoTheme
import tech.blackdeath.contanexo.navegacion.AppRoot

/**
 * Clase principal de la aplicación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContaNexoTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppRoot()
                    }
                }
            }
        }
    }
}