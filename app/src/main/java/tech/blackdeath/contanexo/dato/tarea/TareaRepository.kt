package tech.blackdeath.contanexo.dato.tarea

/**
 * Repositorio de Obligaciones.
 */
interface TareaRepository {
    suspend fun listar(): List<TareaModel>
    suspend fun marcarCompletada(id: String): TareaModel?
    suspend fun adjuntar(id: String, uriOrUrl: String): TareaModel?
}

