package data.domain.rent

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import data.domain.car.Car
import data.domain.user.User

@DatabaseTable(tableName = "Rent", daoClass = RentDaoImpl::class)
class Rent{
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var user : User

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var car: Car
}