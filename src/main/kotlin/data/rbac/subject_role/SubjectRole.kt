package data.rbac.subject_role

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import data.rbac.role.Role
import data.rbac.subject.Subject

@DatabaseTable(tableName = "Subject_Role", daoClass = SubjectRolesDaoImpl::class)
class SubjectRole {
    @DatabaseField(foreign = true, foreignAutoCreate = true)
    lateinit var subject: Subject

    @DatabaseField(foreign = true, foreignAutoCreate = true)
    lateinit var role: Role
}
