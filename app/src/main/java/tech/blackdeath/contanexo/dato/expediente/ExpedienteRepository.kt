package tech.blackdeath.contanexo.dato.expediente

// Punto de integración futuro:
// - Consumir tu backend para obtener URLs pre-firmadas (GET/PUT) y listar documentos por usuario.
// - Opcionalmente, usar Amazon Cognito Identity Pools para firmar peticiones S3 directas.
// - Mantener el bucket privado; no expongas llaves en la app.
interface ExpedienteRepository {
    suspend fun listar(): List<String>
    suspend fun subir(nombre: String, bytes: ByteArray): Boolean
}

class ExpedienteRepositoryMock : ExpedienteRepository {
    override suspend fun listar(): List<String> = listOf("Constancia de Situación Fiscal.pdf")
    override suspend fun subir(nombre: String, bytes: ByteArray): Boolean = true
}