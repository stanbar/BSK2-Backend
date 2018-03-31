package data.domain.user

import com.j256.ormlite.table.DatabaseTable
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id


@DatabaseTable(tableName = "User", daoClass = UserDaoImpl::class)
class User {
    @Id
    @GeneratedValue
    var id: Long = -1

    @Column(nullable = false)
    lateinit var PESEL: String

    @Column(nullable = false)
    lateinit var firstName: String

    @Column(nullable = false)
    lateinit var lastname: String

    @Column(nullable = false)
    var driverLicence: Long = -1
}
