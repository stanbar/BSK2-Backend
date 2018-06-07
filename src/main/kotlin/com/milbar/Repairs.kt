package com.milbar

import com.milbar.service.RepairService
import io.ktor.application.call
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.kodein.di.generic.instance

fun Routing.repairs() {
    val repairService: RepairService by kodein.instance()

    get("/repairs") {
        validateSession(call, "repair:read:*") {
            repairService.getAll()
        }
    }
}