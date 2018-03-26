package service

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import data.role.RoleDao
import data.role.RoleDaoImpl
import data.role.RoleModelMapper
import data.rolepermission.RolePermissionDao
import model.Role

class RoleService(override val kodein: Kodein) : KodeinAware {
    private val rolesDao: RoleDaoImpl = instance()
    private val permissionDao: RolePermissionDao = instance()

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
        val roleEntity = rolesDao.findRoleBy(selector.selector, value) ?: return null

        val role = RoleModelMapper.fromEntity(roleEntity)
        val permissions = permissionDao.getPermissionsForRoleId(role.id)
                .map { it.permission }
        role.permissions.addAll(permissions)
        return role
    }

    fun createRole(name: String, description: String) = rolesDao.createRole(name, description)

    fun deleteRole(roleId: Long) = rolesDao.deleteRole(roleId)

}