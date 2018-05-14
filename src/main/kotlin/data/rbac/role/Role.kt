package data.rbac.role

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable
import data.rbac.rolepermission.RolePermission

@DatabaseTable(tableName = "Role", daoClass = RoleDaoImpl::class)
class Role{
    @DatabaseField(generatedId = true)
    var id: Long = -1

    @DatabaseField(unique = true)
    var name: String = ""

    @DatabaseField
    var description: String = ""

    @ForeignCollectionField(eager = false, maxEagerLevel = 1) //Don't increase since RolePermission.Role will be fetched
    lateinit var permissions: Collection<RolePermission>
}
