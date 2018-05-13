package service

import data.domain.user.User
import data.domain.user.UserDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class UserService(override val kodein: Kodein) : KodeinAware {
    private val userDao: UserDao by instance()
    private val subjectService: SubjectService by instance()
    private val roleService: RoleService by instance()

    fun findUserById(id : Long) : User? = userDao.queryForId(id)
    fun getAllUsers() = userDao.queryForAll()

    @Throws(UserAlreadyExist::class)
    fun createUser(login: String, firstName: String, lastName: String, PESEL: String, driverLicence: Long, password: String): User {
        val subject = subjectService.createSubject(login, password)
        roleService.setDefaultRoleFor(subject)

        val user = User()
        user.subject = subject
        user.firstName = firstName
        user.lastname = lastName
        user.PESEL = PESEL
        user.driverLicence = driverLicence
        userDao.create(user)

        //Test if it went successfully
        assert(user.id != -1L)
        assert(user.subject.id != -1L)
        assert(user.subject.subjectRoles.isNotEmpty())

        return user
    }


}