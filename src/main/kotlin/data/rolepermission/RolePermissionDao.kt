package data.rolepermission

import data.Dao
import data.Database

interface RolePermissionDao {
    fun getPermissionsForRoleId(roleId: Long): Set<RolePermissionEntity>
    fun createPermissionForRoleId(roleId: Long, permission: String): RolePermissionEntity
    fun deletePermissionForRoleId(roleId: Long, permission: String)
}

class RolePermissionDaoImpl(database: Database) : RolePermissionDao, Dao(database) {
    override val TABLE_NAME: String = "Role_Permissions"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (roleId INTEGER NOT NULL, permission TEXT NOT NULL, FOREIGN KEY(roleId) REFERENCES Role(id))"


    override fun getPermissionsForRoleId(roleId: Long): Set<RolePermissionEntity> {
        val list = hashSetOf<RolePermissionEntity>()
        connect().query("SELECT * FROM Role_Permissions WHERE roleId = ?") {
            setLong(1, roleId)
        }.use {
            val resultSet = it.executeQuery()
            while (resultSet.next()) {
                val roleId = resultSet.getLong("roleId")
                val permission = resultSet.getString("permission")
                list.add(RolePermissionEntity(roleId, permission))
            }
        }
        return list
    }

    override fun createPermissionForRoleId(roleId: Long, permission: String): RolePermissionEntity {
        execute("INSERT INTO Role_Permissions(roleId, permission) VALUES ($roleId, \"$permission\")")
        return RolePermissionEntity(roleId, permission)

    }

    override fun deletePermissionForRoleId(roleId: Long, permission: String) {
        execute("DELETE FROM Role_Permissions WHERE roleId = $roleId && permission = $permission)")
    }
}