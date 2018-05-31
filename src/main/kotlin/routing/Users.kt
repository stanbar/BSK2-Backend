package routing

import exception.IllegalParameterException
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import kodein
import org.kodein.di.generic.instance
import service.SubjectService
import service.UserService
import validateSession

fun Routing.users() {
    val subjectService: SubjectService by kodein.instance()
    val userService: UserService by kodein.instance()

    get("/users") {
        validateSession(call, "user:read:*") {
            subjectService.getAll()
        }
    }
    get("/users/{id}") {
        try {
            val id = call.parameters["id"]!!.toLong()
            validateSession(call, "user:read:$id") {
                userService.findById(id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalParameterException(call.parameters["id"])
        }

    }
}