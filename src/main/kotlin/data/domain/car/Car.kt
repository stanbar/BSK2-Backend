package data.domain.car

import com.j256.ormlite.table.DatabaseTable
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id

@DatabaseTable(tableName = "Car", daoClass = CarDaoImpl::class)
class Car {

    @Id
    @GeneratedValue
    var id: Long = -1
    @Column(nullable = false)
    lateinit var brand: String
    @Column(nullable = false)
    lateinit var model: String
    @Column(nullable = false)
    var price: Double = -1.0
}

