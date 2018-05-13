package service

import data.rbac.subject.Subject
import data.rbac.subject.SubjectDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.sql.SQLException

class UserAlreadyExist(login: String) : Throwable("User with login: \"$login\" already exists")
class LoginNotFoundException(login: String) : Throwable("User with login: \"$login\" not found")

class SubjectService(override val kodein: Kodein) : KodeinAware {
    private val subjectDao: SubjectDao by instance()

    fun getAllSubjects(): List<Subject> = subjectDao.queryForAll()

    fun findSubjectById(subjectId: Long): Subject? = subjectDao.queryForId(subjectId)

    @Throws(LoginNotFoundException::class)
    fun findSubjectByLogin(login: String): Subject {
        val result = subjectDao.queryForEq("login", login)
        return if (result.isNotEmpty())
            result.first()
        else throw LoginNotFoundException(login)
    }

    @Throws(SQLException::class, UserAlreadyExist::class)
    fun createSubject(login: String, password: String): Subject {
        try {
            findSubjectByLogin(login)
            throw UserAlreadyExist(login)
        } catch (e: LoginNotFoundException) {
            val subject = Subject()
            subject.login = login
            subject.password = password
            subjectDao.create(subject)
            return subject
        }
    }


}



