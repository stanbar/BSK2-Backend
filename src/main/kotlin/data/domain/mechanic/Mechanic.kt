package data.domain.mechanic

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import data.rbac.subject.Subject
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id

@DatabaseTable(tableName = "Mechanic", daoClass = MechanicDaoImpl::class)
class Mechanic {
    @Id
    @GeneratedValue
    var id: Long = -1

    @DatabaseField(foreign = true, foreignAutoCreate = true)
    lateinit var subject: Subject // representation in RBAC database

    @Column(nullable = false)
    lateinit var firstName: String

    @Column(nullable = false)
    lateinit var lastName: String
}
