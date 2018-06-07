package com.milbar.service

import com.milbar.data.domain.car.Car
import com.milbar.data.domain.car.CarDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class CarService(override val kodein: Kodein) : KodeinAware, Service<Car, CarDao>() {
    enum class Selector(val value: String) {
        ID("id"), BRAND("brand"), MODEL("model")
    }
    override val dao: CarDao by instance()

    fun createCar(brand: String, model: String, price: Double): Car {

        return Car().apply {
            this.brand = brand
            this.model = model
            this.price = price
        }.also { dao.create(it) }
    }


}