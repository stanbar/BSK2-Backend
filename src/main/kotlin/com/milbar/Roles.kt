package com.milbar
import com.milbar.service.RoleService
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.generic.instance

fun Routing.roles() {
    val roleService: RoleService by kodein.instance()

    get("/roles") {
        validateSession(call, "role:read:*") {
            roleService.getAll()
        }
    }
}