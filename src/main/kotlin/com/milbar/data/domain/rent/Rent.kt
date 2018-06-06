package com.milbar.data.domain.rent

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.milbar.data.domain.car.Car
import com.milbar.data.domain.user.User
import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.Id

@DatabaseTable(tableName = "Rent", daoClass = RentDaoImpl::class)
class Rent {
    @Id
    @GeneratedValue
    var id: Long = -1

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var user: User

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    lateinit var car: Car

    @DatabaseField
    lateinit var startDate: Date

    @DatabaseField
    lateinit var endDate: Date
}