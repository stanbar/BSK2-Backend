package data.rbac.subject

import com.j256.ormlite.table.DatabaseTable
import data.rbac.role.Role
import javax.persistence.Column
import javax.persistence.Id

@DatabaseTable(tableName = "Subject", daoClass = SubjectDaoImpl::class)
class Subject{
    @Id
    var id: Long = -1
    @Column
    lateinit var name: String
    @Column
    lateinit var password: String
    @Column
    //TODO test how to presist set
    lateinit var roles: MutableSet<Role>
}
