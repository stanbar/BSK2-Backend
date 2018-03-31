package service

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import data.rbac.role.RoleDao
import data.rbac.role.RoleDaoImpl
import data.rbac.role.RoleModelMapper
import data.rbac.rolepermission.RolePermissionDao
import data.rbac.rolepermission.RolePermissionModelMapper
import data.rbac.subject_role.SubjectRolesDao
import data.rbac.role.Role

class RoleService(override val kodein: Kodein) : KodeinAware {
    private val rolesDao: RoleDaoImpl = instance()
    private val rolePermissionDao: RolePermissionDao = instance()
    private val subjectRoleDao: SubjectRolesDao = instance()

    fun getAllRoles(): List<Role> {
        return rolesDao.getAllRoles().map {
            val role = RoleModelMapper.fromEntity(it)
            val permissions = rolePermissionDao.getPermissionsForRoleId(role.id)
                    .map { RolePermissionModelMapper.fromEntity(it) }
            role.permissions.addAll(permissions)
            role
        }
    }

    fun findRoleById(roleId: Long) = findRoleBy(RoleDao.Selector.ID, roleId)

    fun findRoleByName(roleName: String) = findRoleBy(RoleDao.Selector.NAME, roleName)

    private fun findRoleBy(selector: RoleDao.Selector, value: Any): Role? {
        val roleEntity = rolesDao.findRoleBy(selector.selector, value) ?: return null

        val role = RoleModelMapper.fromEntity(roleEntity)
        val permissions = rolePermissionDao.getPermissionsForRoleId(role.id)
                .map { RolePermissionModelMapper.fromEntity(it) }
        role.permissions.addAll(permissions)
        return role
    }

    fun createRole(name: String, description: String, permissionsStrings: List<String>): Role {
        val role = RoleModelMapper.fromEntity(rolesDao.createRole(name, description))
        val permissions = permissionsStrings.map {
            RolePermissionModelMapper.fromEntity(rolePermissionDao.createPermissionForRoleId(role.id, it))
        }
        role.permissions.addAll(permissions)
        return role
    }

    fun deleteRole(roleId: Long) {
        rolePermissionDao.deleteAllPermissionsForRoleId(roleId)
        subjectRoleDao.deleteWhereRoleId(roleId)
        rolesDao.deleteRole(roleId)
    }

}