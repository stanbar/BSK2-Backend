package data.rbac.rolepermission

import com.j256.ormlite.dao.Dao

interface RolePermissionDao : Dao<RolePermission, Void> {
    fun getPermissionsForRoleId(roleId: Long): Set<RolePermission>
    fun createPermissionForRoleId(roleId: Long, permission: String): RolePermission
    fun deletePermissionForRoleId(roleId: Long, permission: String)
    fun deleteAllPermissionsForRoleId(roleId: Long)
}
