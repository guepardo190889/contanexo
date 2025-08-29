package tech.blackdeath.contanexo.dato.obligacion

import kotlinx.coroutines.delay

/**
 * Repositorio de Obligaciones.
 */
class ObligacionRepositoryMock : ObligacionRepository {
    override suspend fun proximas(limit: Int): List<ObligacionModel> {
        delay(250) // simula IO
        val all = listOf(
            ObligacionModel("Pago IVA agosto", "Vence: 17 Sep 2025"),
            ObligacionModel("Nómina Q2", "Vence: 15 Sep 2025"),
            ObligacionModel("ISR mensual", "Vence: 17 Sep 2025"),
            ObligacionModel("Actualización domicilio fiscal", "Vence: 01 Oct 2025")
        )
        return all.take(limit)
    }
}
