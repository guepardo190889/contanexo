package tech.blackdeath.contanexo.dato.empresa

/**
 * Interfaz de datos de empresa.
 */
interface EmpresaRepository {
    suspend fun listar(): List<EmpresaModel>
}