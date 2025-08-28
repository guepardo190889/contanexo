package tech.blackdeath.contanexo.dato.expediente

/**
 * Modelo de Documento.
 */
data class DocumentoModel(
    val id: Long,
    val nombre: String,
    val categoria: CategoriaDocumento,
    val actualizado: String,
    val key: String,
    val mimeType: String = "application/pdf",
    val urlDirecta: String? = null
)