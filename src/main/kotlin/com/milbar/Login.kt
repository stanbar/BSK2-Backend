package com.milbar

import com.milbar.service.UserService
import com.stasbar.Logger
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import org.apache.shiro.SecurityUtils
import org.kodein.di.generic.instance


fun Route.login() {
    val userService: UserService by kodein.instance()
    get("/logout") {
        logout(call)
    }
    authenticate("basic") {

        get("/myRoles") {
            val currentSubjectId = call.principal<IdPrincipal>()?.id

            if (currentSubjectId != null) {
                val user = userService.findById(currentSubjectId)
                if (user != null) {
                    val response = user.subject.subjectRoles.map {
                        val role = it.role
                        hashMapOf("id" to role.id, "name" to role.name, "description" to role.description)
                    }
                    Logger.d("Roles for user ${user.subject.login}: " + user.subject.subjectRoles.map { it.role.name }.joinToString(","))
                    call.respond(HttpStatusCode.OK, response)
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
            } else
                call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
        }
        post("/login") {
            val selectedRoleId = call.parameters["roleId"]?.toLongOrNull()
            if (selectedRoleId == null) {
                call.respondRedirect("/myRoles", false)
                return@post
            }

            val currentSubjectId = call.principal<IdPrincipal>()?.id
            if (currentSubjectId != null) {
                val user = userService.findById(currentSubjectId)
                if (user != null) {
                    val currentRole = user.subject.subjectRoles.find { it.role.id == selectedRoleId }
                    Logger.d("Logged onto role: "+currentRole?.role?.name)
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
