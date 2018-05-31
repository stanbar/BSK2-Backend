package routing

import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import kodein
import org.kodein.di.generic.instance
import service.RoleService
import validateSession

fun Routing.roles() {
    val roleService: RoleService by kodein.instance()

    get("/roles") {
        validateSession(call, "role:read:*") {
            roleService.getAll()
        }
    }
}