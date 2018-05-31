package routing

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import kodein
import MySession
import IdPrincipal
import org.apache.shiro.SecurityUtils
import org.kodein.di.generic.instance
import service.UserService


fun Route.login() {
    val userService: UserService by kodein.instance()
    authenticate("basic") {
        accept(ContentType.Text.Html) {
            get("/myRoles") {
                val currentSubjectId = call.principal<IdPrincipal>()?.id

                if (currentSubjectId != null) {
                    val user = userService.findById(currentSubjectId)
                    if (user != null) {
                        val roles = user.subject.subjectRoles.map {
                            val role = it.role
                            hashMapOf("id" to role.id, "name" to role.name, "description" to role.description)
                        }

                        call.respond(FreeMarkerContent("myRoles.ftl", mapOf("login" to user.subject.login, "roles" to roles)))
                    } else {
                        call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "Could not find subject after successful login")))

                    }
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
            }
            post("/login") {
                val parameters = call.receiveParameters()
                val selectedRoleId = parameters["roleId"]?.toLongOrNull()
                if (selectedRoleId == null) {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "roleId is null or blank")
                    return@post
                }

                val currentSubjectId = call.principal<IdPrincipal>()?.id
                if (currentSubjectId != null) {
                    val user = userService.findById(currentSubjectId)
                    if (user != null) {
                        val currentRole = user.subject.subjectRoles.find { it.role.id == selectedRoleId }
                        if (currentRole != null) {
                            val sessionId = SecurityUtils.getSubject().session.id as String
                            val mySession = MySession(sessionId, selectedRoleId)

                            call.sessions.set(mySession)

                            //Prevent from exposing all user roles
                            user.subject.subjectRoles = user.subject.subjectRoles.filter { it.role.id == selectedRoleId }
                            call.respond(FreeMarkerContent("user.ftl", mapOf("user" to user)))
                        } else {
                            call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "You don't have role with id \"$selectedRoleId\"")))
                        }

                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
            }
        }
        accept(ContentType.Application.Json) {
            get("/myRoles") {
                val currentSubjectId = call.principal<IdPrincipal>()?.id

                if (currentSubjectId != null) {
                    val user = userService.findById(currentSubjectId)
                    if (user != null) {
                        val response = user.subject.subjectRoles.map {
                            val role = it.role
                            hashMapOf("id" to role.id, "name" to role.name, "description" to role.description)
                        }
                        call.respond(HttpStatusCode.OK, response)
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
            }
            post("/login") {
                val parameters = call.receiveParameters()
                val selectedRoleId = parameters["roleId"]?.toLongOrNull()
                if (selectedRoleId == null) {
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                    call.response.header("Reason", "roleId is null or blank")
                    return@post
                }

                val currentSubjectId = call.principal<IdPrincipal>()?.id
                if (currentSubjectId != null) {
                    val user = userService.findById(currentSubjectId)
                    if (user != null) {
                        val currentRole = user.subject.subjectRoles.find { it.role.id == selectedRoleId }
                        if (currentRole != null) {
                            val sessionId = SecurityUtils.getSubject().session.id as String
                            val mySession = MySession(sessionId, selectedRoleId)

                            call.sessions.set(mySession)

                            //Prevent from exposing all user roles
                            user.subject.subjectRoles = user.subject.subjectRoles.filter { it.role.id == selectedRoleId }
                            call.respond(HttpStatusCode.OK, user)
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, "You don't have role with id \"$selectedRoleId\"")
                        }

                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
            }

        }
    }
}