package com.milbar
import com.milbar.exception.IllegalParameterException
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.generic.instance
import com.milbar.service.RoleService
import com.milbar.service.SubjectService
import com.milbar.service.UserService

fun Routing.permissions(){
    val subjectService: SubjectService by kodein.instance()
    val userService: UserService by kodein.instance()

    //TODO remove it, security leak
    get("/grand/{subjectId}/{roleId}") {
        try {
            val subjectId = call.parameters["subjectId"]!!.toLong()
            val roleId = call.parameters["roleId"]!!.toLong()

            val roleService: RoleService by kodein.instance()
            val role = roleService.findById(roleId)
            val subject = subjectService.findById(subjectId)
            roleService.addRoleToSubject(role!!, subject!!)
            call.respond(subjectService.findById(subjectId)!!)

        } catch (e: Exception) {
            throw IllegalParameterException("${call.parameters["subjectId"]} and ${call.parameters["roleId"]}")
        }

    }

}