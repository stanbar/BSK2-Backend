package routing

import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.contentType
import kodein
import org.kodein.di.generic.instance
import service.SubjectService
import service.UserService

@Location("/signup")
class Signup(val login: String, val password: String, val firstName: String, val lastName: String, val driverLicence: String, val PESEL: String)

fun Route.signup() {
    val subjectService: SubjectService by kodein.instance()
    val userService: UserService by kodein.instance()

    contentType(ContentType.Text.Html) {
        post<Signup> { signup ->
            with(signup) {
                when {
                    login.isNullOrBlank() -> {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "login is null or blank")))
                    }
                    password.isNullOrBlank() -> {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "password is null or blank")))
                    }
                    firstName.isNullOrBlank() -> {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "firstName is null or blank")))
                    }
                    lastName.isNullOrBlank() -> {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "lastName is null or blank")))
                    }
                    driverLicence.isNullOrBlank() -> {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "driverLicence is null or blank")))
                    }
                    PESEL.isNullOrBlank() -> {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "PESEL is null or blank")))
                    }
                    subjectService.findBy(SubjectService.Selector.LOGIN.value, login) != null -> call.respond(HttpStatusCode.Conflict, "Subject with this login is already created")
                    else -> {
                        call.respond(HttpStatusCode.OK, userService.createUser(login, password, firstName, lastName, PESEL, driverLicence))
                    }
                }
            }

        }
    }

    post<Signup> { signup ->
        with(signup) {
            when {
                login.isNullOrBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "login is null or blank")
                    return@post
                }
                password.isNullOrBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "password is null or blank")
                    return@post
                }
                firstName.isNullOrBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "firstName is null or blank")
                    return@post
                }
                lastName.isNullOrBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "lastName is null or blank")
                    return@post
                }
                driverLicence.isNullOrBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "driverLicence is null or blank")
                    return@post
                }
                PESEL.isNullOrBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "PESEL is null or blank")
                    return@post
                }
                subjectService.findBy(SubjectService.Selector.LOGIN.value, login) != null -> call.respond(HttpStatusCode.Conflict, "Subject with this login is already created")
                else -> {
                    val createdUser = userService.createUser(login, password, firstName, lastName, PESEL, driverLicence)
                    call.respond(HttpStatusCode.OK, createdUser)
                }
            }
        }

    }

}