package tech.blackdeath.contanexo.dato.aviso

import kotlinx.coroutines.delay

/**
 * Repositorio de Avisos.
 */
class AvisoRepositoryMock : AvisoRepository {
    override suspend fun recientes(limit: Int): List<AvisoModel> {
        delay(250) // simula IO
        val all = listOf(
            AvisoModel("Nueva obligación", "Declaración mensual lista para revisión"),
            AvisoModel("Documento subido", "Carta de contribuyente cumplido"),
            AvisoModel("Recordatorio", "Revisa CFDIs de agosto")
        )
        return all.take(limit)
    }
}
