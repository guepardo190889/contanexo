package tech.blackdeath.contanexo.dato.aviso

import kotlinx.coroutines.delay

/**
 * Repositorio de Avisos.
 */
class AvisoRepositoryMock : AvisoRepository {
    override suspend fun recientes(limit: Int): List<AvisoModel> {
        delay(250) // simula IO
        val all = listOf(
            AvisoModel(1L, "Nueva obligación", "Declaración mensual lista para revisión", "2025-08-20", false),
            AvisoModel(2L, "Documento subido", "Carta de contribuyente cumplido", "2025-08-18", true),
            AvisoModel(3L, "Recordatorio", "Revisa CFDIs de agosto", "2025-08-15", false),
            AvisoModel(4L, "Aviso SAT", "Actualiza domicilio fiscal", "2025-07-30", true),
            AvisoModel(5L, "Nómina", "Carga nómina quincena 2", "2025-08-22", false)
        )
        return all.take(limit)
    }
}
