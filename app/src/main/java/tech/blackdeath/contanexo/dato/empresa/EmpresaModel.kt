package tech.blackdeath.contanexo.dato.empresa

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Modelo de datos de empresa.
 */
data class EmpresaModel(
    val id: String,
    val nombre: String,
    val rfc: String,
    val avatar: String? = null,
    val favorita: Boolean = false
)

/**
 * Estado de UI de empresa.
 */
@Stable
class EmpresaState(initial: EmpresaModel?) {
    var actual by mutableStateOf(initial)
}

/**
 * Crea el estado de UI de empresa.
 */
@Composable
fun rememberEmpresaState(initial: EmpresaModel? = null) = remember {
    EmpresaState(initial)
}