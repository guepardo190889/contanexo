package tech.blackdeath.contanexo.dato.aviso

/**
 * Repositorio de Avisos.
 */
interface AvisoRepository {
    suspend fun recientes(limit: Int = 50): List<AvisoModel>
}