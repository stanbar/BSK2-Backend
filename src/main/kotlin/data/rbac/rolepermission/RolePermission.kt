package data.rbac.rolepermission

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import data.rbac.role.Role
@DatabaseTable(tableName = "Role_Permission", daoClass = RolePermissionDaoImpl::class)
class RolePermission{
    @DatabaseField(foreign = true)
    lateinit var role: Role

    @DatabaseField(canBeNull = false)
    lateinit var permission : String
}