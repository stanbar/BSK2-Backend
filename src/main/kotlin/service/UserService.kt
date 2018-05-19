package service

import Permission
import data.domain.user.User
import data.domain.user.UserDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import service.exception.SubjectAlreadyExist

class UserService(override val kodein: Kodein) : KodeinAware, Service<User, UserDao>() {
    override val dao: UserDao by instance()
    private val subjectService: SubjectService by instance()
    private val roleService: RoleService by instance()

    @Throws(SubjectAlreadyExist::class)
    fun createUser(login: String, password: String, firstName: String, lastName: String, PESEL: String, driverLicence: String): User {
        val subject = subjectService.createSubject(login, password)

        val user = User()
        user.subject = subject
        user.firstName = firstName
        user.lastName = lastName
        user.PESEL = PESEL
        user.driverLicence = driverLicence
        dao.create(user)

        //Test if it went successfully
        assert(user.id != -1L)
        assert(user.subject.id != -1L)

        val role = roleService.setDefaultRoleFor(subject)
        roleService.createPermissionForRole(Permission.from(Permission.Domain.USER, Permission.Action.READ, user.id), role)

        dao.refresh(user)

        return user
    }

}