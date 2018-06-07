package com.milbar.service

import com.milbar.data.domain.repair.Repair
import com.milbar.data.domain.repair.RepairDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class RepairService(override val kodein: Kodein) : KodeinAware, Service<Repair, RepairDao>() {
    enum class Selector(val value: String) {
        ID("id")
    }

    override val dao: RepairDao by instance()
    private val carService: CarService by instance()
    private val mechanicService: MechanicService by instance()

    fun createRepair(carId: Long, mechanicId: Long): Repair {
        val car = carService.findById(carId)!!
        val mechanic = mechanicService.findById(mechanicId)!!


        return Repair().apply {
            this.car = car
            this.mechanic = mechanic
        }.also { dao.create(it) }
    }


}
