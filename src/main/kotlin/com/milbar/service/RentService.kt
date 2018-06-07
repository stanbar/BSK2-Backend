package com.milbar.service

import com.milbar.data.domain.rent.Rent
import com.milbar.data.domain.rent.RentDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.util.*

class RentService(override val kodein: Kodein) : KodeinAware, Service<Rent, RentDao>() {
    enum class Selector(val value: String) {
        ID("id")
    }

    override val dao: RentDao by instance()
    private val userService: UserService by instance()
    private val carService: CarService by instance()

    fun createRent(userId: Long, carId: Long, startDate: Date, endDate: Date): Rent {
        val user = userService.findById(userId)!!
        val car = carService.findById(carId)!!

        return Rent().apply {
            this.car = car
            this.user = user
            this.startDate= startDate
            this.endDate = endDate
        }.also { dao.create(it) }
    }


}