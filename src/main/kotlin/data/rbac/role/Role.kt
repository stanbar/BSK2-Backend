package data.rbac.role

import com.j256.ormlite.table.DatabaseTable
import data.rbac.role.RoleDaoImpl

@DatabaseTable(tableName = "Role", daoClass = RoleDaoImpl::class)
class Role(
        val id: Long,
        val name: String,
        val description: String,
        val permissions: MutableSet<String> = hashSetOf())