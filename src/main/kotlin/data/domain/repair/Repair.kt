package data.domain.repair

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import data.domain.car.Car
import data.domain.mechanic.Mechanic
import javax.persistence.Entity


@DatabaseTable(tableName = "Repair", daoClass = RepairDaoImpl::class)
class Repair {

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var mechanic: Mechanic

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var car: Car
}
