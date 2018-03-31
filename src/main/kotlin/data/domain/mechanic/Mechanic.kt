package data.domain.mechanic

import com.j256.ormlite.table.DatabaseTable
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id

@DatabaseTable(tableName = "Mechanic", daoClass = MechanicDaoImpl::class)
class Mechanic {
    @Id
    @GeneratedValue
    var id: Long = -1
    @Column(nullable = false)
    lateinit var firstName: String
    @Column(nullable = false)
    lateinit var lastName: String
}
