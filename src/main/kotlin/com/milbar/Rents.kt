package com.milbar

import com.milbar.service.RentService
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.generic.instance

fun Routing.rents() {
    val rentService: RentService by kodein.instance()

    get("/rents") {
        validateSession(call, "rent:read:*") {
            rentService.getAll()
        }
    }
}