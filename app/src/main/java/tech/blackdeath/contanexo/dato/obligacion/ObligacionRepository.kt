package tech.blackdeath.contanexo.dato.obligacion

/**
 * Repositorio de Obligaciones.
 */
interface ObligacionRepository {
    suspend fun proximas(limit: Int = 5): List<ObligacionModel>
}

