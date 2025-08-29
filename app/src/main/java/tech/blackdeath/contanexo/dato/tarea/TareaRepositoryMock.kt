package tech.blackdeath.contanexo.dato.tarea

import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Repositorio de Tareas.
 */
class TareaRepositoryMock : TareaRepository {

    companion object {
        private val data = ConcurrentHashMap<String, TareaModel>().apply {
            val now = Instant.now().toEpochMilli()
            // Ejemplos variados
            put("T1", TareaModel(
                id = "T1",
                titulo = "IVA Agosto 2025",
                tipo = TareaTipo.OBLIGACION,
                descripcionCorta = "Presenta y paga el IVA del periodo AGO-2025.",
                descripcionLarga = "Reúne CFDI ingresos/egresos y presenta el IVA del periodo AGO-2025. " +
                        "Si ya lo pagaste en portal SAT, adjunta acuse/pago para registro.",
                obligacionTag = "IVA mensual",
                venceElUtc = ZonedDateTime.of(2025, 9, 17, 23, 59, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli(),
                estado = TareaEstado.PENDIENTE,
                prioridad = Prioridad.ALTA,
                enlaces = listOf("https://www.sat.gob.mx/")
            ))
            put("T2", TareaModel(
                id = "T2",
                titulo = "Carga CFDI de gastos agosto",
                tipo = TareaTipo.SOLICITUD,
                descripcionCorta = "Sube los CFDI de gastos para deducibles.",
                descripcionLarga = "Carga en PDF/XML los CFDI de gastos del mes para su conciliación.",
                obligacionTag = null,
                venceElUtc = ZonedDateTime.of(2025, 8, 25, 18, 0, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli(),
                estado = TareaEstado.PENDIENTE,
                prioridad = Prioridad.MEDIA
            ))
            put("T3", TareaModel(
                id = "T3",
                titulo = "Revisar nómina quincena 2",
                tipo = TareaTipo.REVISION,
                descripcionCorta = "Valida percepciones/deducciones antes de timbrar.",
                descripcionLarga = "Revisa los conceptos de nómina y confirma si todo es correcto.",
                obligacionTag = "Nómina",
                venceElUtc = now - 86_400_000L, // ayer -> debería verse VENCIDA
                estado = TareaEstado.PENDIENTE,
                prioridad = Prioridad.ALTA
            ))
            put("T4", TareaModel(
                id = "T4",
                titulo = "Opinión de cumplimiento SAT",
                tipo = TareaTipo.VISTO_BUENO,
                descripcionCorta = "Descarga/revisa la opinión de cumplimiento.",
                descripcionLarga = "La opinión de cumplimiento está disponible para descargar y validar.",
                obligacionTag = "Opinión de cumplimiento",
                venceElUtc = null,
                estado = TareaEstado.COMPLETADA,
                prioridad = Prioridad.BAJA,
                adjuntos = listOf("https://ejemplo.s3.amazonaws.com/acuse-opinion.pdf"),
                enlaces = listOf("https://www.sat.gob.mx/")
            ))
        }

        private fun update(id: String, transform: (TareaModel) -> TareaModel?): TareaModel? {
            val current = data[id] ?: return null
            val updated = transform(current) ?: return null
            data[id] = updated
            return updated
        }
    }

    override suspend fun listar(): List<TareaModel> {
        delay(120)
        val now = Instant.now().toEpochMilli()
        return data.values
            .map { t ->
                if (t.estado == TareaEstado.PENDIENTE && t.venceElUtc != null && t.venceElUtc < now) {
                    t.copy(estado = TareaEstado.VENCIDA)
                } else t
            }
            .sortedWith(
                compareBy<TareaModel> { it.estado != TareaEstado.VENCIDA } // Vencidas arriba
                    .thenByDescending { it.prioridad }
                    .thenBy { it.venceElUtc ?: Long.MAX_VALUE }
            )
    }

    override suspend fun marcarCompletada(id: String): TareaModel? {
        delay(60)
        return update(id) { it.copy(estado = TareaEstado.COMPLETADA) }
    }

    override suspend fun adjuntar(id: String, uriOrUrl: String): TareaModel? {
        delay(60)
        return update(id) { it.copy(adjuntos = it.adjuntos + uriOrUrl) }
    }
}
