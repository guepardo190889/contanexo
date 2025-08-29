package tech.blackdeath.contanexo.dato.tarea

/**
 * Modelo de datos de una tarea.
 */
data class TareaModel(
    val id: String,
    val titulo: String,
    val tipo: TareaTipo,
    val descripcionCorta: String? = null,
    val descripcionLarga: String? = null,     // se muestra en el modal
    val obligacionTag: String? = null,        // ej. "IVA mensual", "DIOT"
    val venceElUtc: Long? = null,             // millis UTC; null si no aplica
    val estado: TareaEstado = TareaEstado.PENDIENTE,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val adjuntos: List<String> = emptyList(), // URIs (content://, file://) o URLs (https://)
    val enlaces: List<String> = emptyList()   // ligas relacionadas (SAT/SIPARE/etc.)
)

enum class TareaTipo { OBLIGACION, SOLICITUD, REVISION, VISTO_BUENO }
enum class TareaEstado { PENDIENTE, COMPLETADA, VENCIDA, CANCELADA }
enum class Prioridad { BAJA, MEDIA, ALTA }
