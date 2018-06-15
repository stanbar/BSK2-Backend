package com.milbar

import com.milbar.service.RentService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import org.kodein.di.generic.instance
import java.text.ParseException
import java.text.SimpleDateFormat

@Location("/rents")
class Rents(val userId: Long, val carId: Long, val from: String, val to: String)

fun Routing.rents() {
    val rentService: RentService by kodein.instance()

    get("/rents") {
        validateSession(call, "rent:read:*") {
            rentService.getAll()
        }
    }
    post("/rents") {
        val rents = call.receive<Rents>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

        val from = try {
            dateFormat.parse(rents.from)
        } catch (e: ParseException) {
            e.printStackTrace()
            call.response.status(HttpStatusCode.NonAuthoritativeInformation)
            call.response.header("Reason", "from has wrong format, you need to pass ${dateFormat.toPattern()}")
            return@post
        }
        val to = try {
            dateFormat.parse(rents.to)
        } catch (e: ParseException) {
            e.printStackTrace()
            call.response.status(HttpStatusCode.NonAuthoritativeInformation)
            call.response.header("Reason", "to has wrong format, you need to pass ${dateFormat.toPattern()}")
            return@post
        }



        validateSession(call, "rent:create:*") {
            rentService.createRent(
                    rents.userId,
                    rents.carId,
                    from,
                    to)
        }

    }


}