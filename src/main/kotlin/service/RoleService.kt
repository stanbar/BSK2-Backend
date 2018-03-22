package service

import data.role.RoleDao
import data.role.RoleModelMapper
import data.rolepermission.RolePermissionDao
import model.Role

class RoleService(val rolesDao: RoleDao, val permissionDao: RolePermissionDao) {

    fun getAllRoles(): List<Role> {
        return rolesDao.getAllRoles().map {
            val role = RoleModelMapper.fromEntity(it)
            val permissions = permissionDao.getPermissionsForRoleId(role.id)
                    .map { it.permission }
            role.permissions.addAll(permissions)
            role
        }
    }
    fun findRoleById(roleId: Long) = findRoleBy(RoleDao.Selector.ID, roleId)

    fun findRoleByName(roleName: String) = findRoleBy(RoleDao.Selector.NAME, roleName)

    private fun findRoleBy(selector: RoleDao.Selector, value: Any): Role? {
        val roleEntity = rolesDao.findRoleBy(selector, value) ?: return null

        val role = RoleModelMapper.fromEntity(roleEntity)
        val permissions = permissionDao.getPermissionsForRoleId(role.id)
                .map { it.permission }
        role.permissions.addAll(permissions)
        return role
    }

    fun createRole(name: String, description: String) = rolesDao.createRole(name, description)

    fun deleteRole(roleId: Long) = rolesDao.deleteRole(roleId)

}