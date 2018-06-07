package com.milbar

import com.milbar.service.CarService
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.generic.instance

fun Routing.cars() {
    val carService: CarService by kodein.instance()

    get("/cars") {
        validateSession(call, "car:read:*") {
            carService.getAll()
        }
    }
}