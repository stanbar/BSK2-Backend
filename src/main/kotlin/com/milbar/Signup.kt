package com.milbar
import com.milbar.service.SubjectService
import com.milbar.service.UserService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import org.kodein.di.generic.instance

@Location("/signup")
class Signup(val login: String, val password: String, val firstName: String, val lastName: String, val driverLicence: String, val PESEL: String)

fun Route.signup() {
    val subjectService: SubjectService by kodein.instance()
    val userService: UserService by kodein.instance()

    post<Signup> { signup ->
        with(signup) {
            when {
                login.isBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "login is null or blank")
                    return@post
                }
                password.isBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "password is null or blank")
                    return@post
                }
                firstName.isBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "firstName is null or blank")
                    return@post
                }
                lastName.isBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "lastName is null or blank")
                    return@post
                }
                driverLicence.isBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "driverLicence is null or blank")
                    return@post
                }
                PESEL.isBlank() -> {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "PESEL is null or blank")
                    return@post
                }
                subjectService.findBy(SubjectService.Selector.LOGIN.value, login) != null ->
                    call.respond(HttpStatusCode.Conflict, "Subject with this login is already created")
                else -> {
                    val createdUser = userService.createUser(login, password, firstName, lastName, PESEL, driverLicence)
                    call.respond(HttpStatusCode.OK, createdUser)
                }
            }
        }

    }

}