package tech.blackdeath.contanexo.dato.aviso

import kotlinx.coroutines.delay

/**
 * Repositorio de Avisos.
 */
class AvisoRepositoryMock : AvisoRepository {
    override suspend fun recientes(limit: Int): List<AvisoModel> {
        delay(250) // simula IO
        val all = listOf(
            AvisoModel(
                id = 1L,
                titulo = "Declaración mensual lista para revisión",
                detalle = "Tu contador dejó lista la declaración de agosto.",
                descripcionLarga = """
                    Tu contador preparó la declaración mensual de impuestos correspondiente al periodo AUG-2025.
                    Por favor revisa los importes de IVA e ISR y confirma para su envío.
                    
                    Si tienes dudas, responde a este aviso y las aclaramos.
                """.trimIndent(),
                fechaIso = "2025-08-22",
                leido = false,
                categoria = "Impuestos",
                prioridad = AvisoPrioridad.ALTA,
                origen = AvisoOrigen.CONTADOR,
                enlace = "https://www.sat.gob.mx/"
            ),
            AvisoModel(
                id = 2L,
                titulo = "Carta de contribuyente cumplido disponible",
                detalle = "Se cargó al expediente la carta de contribuyente cumplido.",
                descripcionLarga = """
                    Hemos subido al expediente la Carta de Contribuyente Cumplido.
                    La puedes descargar desde la sección Expediente > Reconocimientos.
                """.trimIndent(),
                fechaIso = "2025-08-18",
                leido = true,
                categoria = "Expediente",
                prioridad = AvisoPrioridad.BAJA,
                origen = AvisoOrigen.SISTEMA,
                enlace = null
            ),
            AvisoModel(
                id = 3L,
                titulo = "Recordatorio: CFDI de gastos de agosto",
                detalle = "Carga los comprobantes de gastos para deducciones.",
                descripcionLarga = """
                    Recuerda cargar antes del 25 de agosto los CFDI de gastos y servicios para su deducción.
                    Esto ayuda a optimizar tu base gravable y evitar diferencias con el SAT.
                """.trimIndent(),
                fechaIso = "2025-08-15",
                leido = false,
                categoria = "Administrativo",
                prioridad = AvisoPrioridad.MEDIA,
                origen = AvisoOrigen.SISTEMA
            ),
            AvisoModel(
                id = 4L,
                titulo = "SAT: Actualiza domicilio fiscal",
                detalle = "Se detectó que tu domicilio fiscal requiere actualización.",
                descripcionLarga = """
                    El SAT marcó tu domicilio fiscal como pendiente de confirmar.
                    Ingresa al portal del SAT para actualizarlo y evita multas.
                """.trimIndent(),
                fechaIso = "2025-07-30",
                leido = true,
                categoria = "SAT",
                prioridad = AvisoPrioridad.ALTA,
                origen = AvisoOrigen.SAT,
                enlace = "https://www.sat.gob.mx/"
            )
        )
        return all.take(limit)
    }
}
