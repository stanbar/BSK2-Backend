package service

import data.domain.mechanic.Mechanic
import data.domain.mechanic.MechanicDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class MechanicService(override val kodein: Kodein) : KodeinAware, Service<Mechanic, MechanicDao>() {
    enum class Selector(val value: String) {
        ID("id"), FIRST_NAME("firstName"), SECOND_NAME("lastName")
    }
    override val dao: MechanicDao by instance()
    private val subjectService: SubjectService by instance()
    private val roleService: RoleService by instance()

    @Throws(SubjectAlreadyExist::class)
    fun createMechanic(login: String, firstName: String, lastName: String, password: String): Mechanic {
        val subject = subjectService.createSubject(login, password)
        assert(subject.id != -1L)
        roleService.setDefaultRoleFor(subject)
        assert(subject.subjectRoles.isNotEmpty())

        val mechanic = Mechanic().apply {
            this.subject = subject
            this.firstName = firstName
            this.lastName = lastName
        }.also {
            dao.create(it)
        }
        assert(mechanic.id != -1L)

        return mechanic
    }


}