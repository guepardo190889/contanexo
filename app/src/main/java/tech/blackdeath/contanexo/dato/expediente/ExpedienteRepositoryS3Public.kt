package tech.blackdeath.contanexo.dato.expediente

import android.net.Uri

/**
 * Repositorio de Expediente.
 */
class ExpedienteRepositoryS3Public(
    private val baseUrl: String = "https://contanexo.s3.us-east-2.amazonaws.com/"
) : ExpedienteRepository {
    override suspend fun listar(): List<DocumentoModel> = listOf(
        DocumentoModel(
            id = 1L,
            nombre = "Constancia de Situación Fiscal.pdf",
            categoria = CategoriaDocumento.FISCAL,
            actualizado = "10 Ago 2025",
            key = "expediente/CSF_2025.pdf",
            mimeType = "application/pdf"
        ),
        DocumentoModel(
            id = 2L,
            nombre = "RFC.pdf",
            categoria = CategoriaDocumento.FISCAL,
            actualizado = "01 Dic 2024",
            key = "expediente/RFC.pdf",
            mimeType = "application/pdf"
        ),
        DocumentoModel(
            id = 3L,
            nombre = "Acta constitutiva.pdf",
            categoria = CategoriaDocumento.LEGAL,
            actualizado = "20 Jun 2018",
            key = "expediente/Acta_constitutiva.pdf",
            mimeType = "application/pdf"
        ),
        DocumentoModel(
            id = 4L,
            nombre = "Opinión de cumplimiento.pdf",
            categoria = CategoriaDocumento.FISCAL,
            actualizado = "05 Ago 2025",
            key = "expediente/Opinion_cumplimiento_2025.pdf",
            mimeType = "application/pdf"
        ))

    override suspend fun urlDescarga(key: String, urlDirecta: String?): String {
        return urlDirecta ?: s3PublicUrl(key = key)
    }

    override suspend fun subir(nombre: String, bytes: ByteArray): Boolean {
        TODO("Not yet implemented")
    }
}

private fun s3PublicUrl(
    bucket: String = "contanexo",
    region: String = "us-east-2",
    key: String
): String {
    // Construimos https://contanexo.s3.us-east-2.amazonaws.com/expediente/CSF_2025.pdf
    // codificando cada segmento (soporta espacios, acentos, etc.).
    val builder = Uri.Builder()
        .scheme("https")
        .authority("$bucket.s3.$region.amazonaws.com")
    key.split("/").forEach { segment ->
        builder.appendPath(segment)
    }
    return builder.build().toString()
}