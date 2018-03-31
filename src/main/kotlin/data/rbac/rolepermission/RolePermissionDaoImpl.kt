package data.rbac.rolepermission

import com.github.salomonbrys.kodein.Kodein
import data.Dao

class RolePermissionDaoImpl(kodein: Kodein) : RolePermissionDao, Dao(kodein) {


    override val tableName: String = "Role_Permissions"
    override val create: String = "create TABLE $tableName (permission TEXT NOT NULL, roleId INTEGER NOT NULL, FOREIGN KEY(roleId) REFERENCES Role(id))"


    override fun getPermissionsForRoleId(roleId: Long): Set<RolePermissionEntity> {
        val list = hashSetOf<RolePermissionEntity>()
        connect().query("SELECT * FROM Role_Permissions WHERE roleId = ?") {
            setLong(1, roleId)
        }.use {
            val resultSet = it.executeQuery()
            while (resultSet.next()) {
                val permission = resultSet.getString("permission")
                list.add(RolePermissionEntity(permission,roleId ))
            }
        }
        return list
    }

    override fun createPermissionForRoleId(roleId: Long, permission: String): RolePermissionEntity {
        execute("INSERT INTO Role_Permissions(permission, roleId) VALUES ($roleId, \"$permission\")")
        return RolePermissionEntity(permission, roleId )

    }

    override fun deletePermissionForRoleId(roleId: Long, permission: String) {
        execute("DELETE FROM Role_Permissions WHERE roleId = $roleId && permission = $permission)")
    }
    override fun deleteAllPermissionsForRoleId(roleId: Long) {
        execute("DELETE FROM Role_Permissions WHERE roleId = $roleId)")
    }
}