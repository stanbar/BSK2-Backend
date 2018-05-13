package data.domain.repair

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import data.domain.car.Car
import data.domain.mechanic.Mechanic
import javax.persistence.GeneratedValue
import javax.persistence.Id


@DatabaseTable(tableName = "Repair", daoClass = RepairDaoImpl::class)
class Repair {
    @Id
    @GeneratedValue
    var id: Long = -1

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var mechanic: Mechanic

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var car: Car
}
