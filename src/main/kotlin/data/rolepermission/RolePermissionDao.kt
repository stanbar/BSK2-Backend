package data.rolepermission

interface RolePermissionDao {
    fun recreate()
    fun getPermissionsForRoleId(roleId: Long): Set<RolePermissionEntity>
    fun createPermissionForRoleId(roleId: Long, permission: String): RolePermissionEntity
    fun deletePermissionForRoleId(roleId: Long, permission: String)
}
