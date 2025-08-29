// app/src/main/java/tech/blackdeath/contanexo/interfaz/pantalla/IngresoPantalla.kt
package tech.blackdeath.contanexo.interfaz.pantalla

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.blackdeath.contanexo.navegacion.LocalAppNavigator
import tech.blackdeath.contanexo.navegacion.Pantalla

/**
 * Pantalla de inicio de sesión.
 */
@Composable
fun IngresoPantalla(
    modifier: Modifier = Modifier,
    onLogin: (() -> Unit)? = null
) {
    val nav = LocalAppNavigator.current
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarPass by remember { mutableStateOf(false) }
    var recordarme by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    fun validarCampos(): Boolean {
        if (usuario.isBlank() || contrasena.isBlank()) {
            error = "Ingresa usuario y contraseña."
            return false
        }
        error = null
        return true
    }

    fun doLogin() {
        if (!validarCampos() || cargando) return
        focus.clearFocus()
        cargando = true
        scope.launch {
            // Simulación breve de autenticación
            delay(600)
            cargando = false
            // Aquí conectar con tu Auth real; si falla, setea error.
            // Por ahora, éxito:
            if (onLogin != null) {
                onLogin()
            } else {
                nav.go(Pantalla.Inicio)
            }
        }
    }

    val gradiente = Brush.verticalGradient(
        0f to MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        1f to MaterialTheme.colorScheme.surface
    )

    Scaffold { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(gradiente)
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                // Encabezado corporativo
                Text(
                    text = "ContaNexo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Acceso a tu panel fiscal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Card de login
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = usuario,
                            onValueChange = { usuario = it },
                            label = { Text("Usuario o correo") },
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = { contrasena = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                            singleLine = true,
                            visualTransformation = if (mostrarPass) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { mostrarPass = !mostrarPass }) {
                                    Text(if (mostrarPass) "Ocultar" else "Mostrar")
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = recordarme, onCheckedChange = { recordarme = it })
                            Text("Recordar sesión", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { /* TODO: flujo recuperar */ }) {
                                Text("¿Olvidaste tu contraseña?")
                            }
                        }

                        if (error != null) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = ::doLogin,
                            enabled = !cargando && usuario.isNotBlank() && contrasena.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Entrando…")
                            } else {
                                Text("Entrar")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Pie simple corporativo
                Text(
                    text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} ContaNexo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
