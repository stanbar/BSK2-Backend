package com.milbar

import com.milbar.service.MechanicService
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.generic.instance

fun Routing.mechanics() {
    val mechanicService: MechanicService by kodein.instance()

    get("/mechanics") {
        validateSession(call, "mechanic:read:*") {
            mechanicService.getAll()
        }
    }
}