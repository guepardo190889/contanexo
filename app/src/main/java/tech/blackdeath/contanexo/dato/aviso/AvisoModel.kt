package tech.blackdeath.contanexo.dato.aviso

/**
 * Modelo de datos de un Aviso.
 */
data class AvisoModel(
    val id: Long,
    val titulo: String,
    val detalle: String,           // resumen corto que aparece en la lista
    val descripcionLarga: String,  // cuerpo extendido para el modal
    val fechaIso: String,          // ej. "2025-08-22"
    val leido: Boolean,
    val categoria: String? = null, // p.ej. "Impuestos", "Nómina", "SAT"
    val prioridad: AvisoPrioridad = AvisoPrioridad.MEDIA,
    val origen: AvisoOrigen = AvisoOrigen.SISTEMA,
    val enlace: String? = null     // si hay un link relacionado (SAT, documento, etc.)
)

enum class AvisoPrioridad { BAJA, MEDIA, ALTA }
enum class AvisoOrigen { CONTADOR, SAT, SISTEMA }
