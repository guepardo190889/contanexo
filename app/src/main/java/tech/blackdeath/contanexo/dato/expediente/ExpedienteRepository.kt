package tech.blackdeath.contanexo.dato.expediente

/**
 * Interfaz de repositorio de Expediente.
 */
interface ExpedienteRepository {
    suspend fun listar(): List<DocumentoModel>
    suspend fun urlDescarga(key: String, urlDirecta: String? = null): String
    suspend fun subir(nombre: String, bytes: ByteArray): Boolean
}
