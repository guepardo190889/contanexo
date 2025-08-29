package tech.blackdeath.contanexo.dato.empresa

/**
 * Mock de datos de empresa.
 */
class EmpresaRepositoryMock : EmpresaRepository {
    override suspend fun listar() = listOf(
        EmpresaModel("1","Grupo Orquídea SA de CV","GOS000101XXX", favorita = true),
        EmpresaModel("2","Servicios Delta SC","SDS090202YYY"),
        EmpresaModel("3","Construcciones Alfa SA","CAS050303ZZZ"),
        EmpresaModel("4","Cafetería La Esquina","CLE180101AAA")
    )
}