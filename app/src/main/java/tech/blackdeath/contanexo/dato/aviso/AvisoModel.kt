package tech.blackdeath.contanexo.dato.aviso

/**
 * Modelo de datos de un Aviso.
 */
data class AvisoModel(
    val id: Long,
    val titulo: String,
    val detalle: String,
    val fechaIso: String,   // ej. "2025-08-10"
    val leido: Boolean
)

