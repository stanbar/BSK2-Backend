package service

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import data.role.RoleDao
import data.role.RoleModelMapper
import data.rolepermission.RolePermissionDao
import data.user.UserDao
import data.user.UserModelMapper
import data.userrole.UserRolesDao
import model.User

class UserService(override val kodein: Kodein) : KodeinAware {
    private val userDao: UserDao = instance()
    private val rolesDao: RoleDao = instance()
    private val userRolesDao: UserRolesDao = instance()
    private val rolePermissionDao: RolePermissionDao = instance()

    fun findUserById(userId: Long) = findUserBy(UserDao.Selector.ID, userId)

    fun findUserByName(username: String) = findUserBy(UserDao.Selector.USERNAME, username)

    private fun findUserBy(selector: UserDao.Selector, value: Any): User? {
        val userEntity = userDao.findUserBy(selector.selector, value) ?: return null
        val user = UserModelMapper.fromEntity(userEntity)
        fillUserRoles(user)
        return user
    }

    fun getAllUsers(): List<User> {
        return userDao.getAllUsers().map {
            val user = UserModelMapper.fromEntity(it)
            fillUserRoles(user)
            user
        }
    }

    private fun fillUserRoles(user: User) {
        val userRoles = userRolesDao.getRolesForUserId(user.id)
        val roles = userRoles.map {
            val roleEntity = rolesDao.findRoleById(it.roleId)
                    ?: throw NullPointerException("Could not find role entity of id ${it.roleId}")

            val newPermissions = rolePermissionDao.getPermissionsForRoleId(roleEntity.id)
                    .map { it.permission }

            RoleModelMapper.fromEntity(roleEntity)
                    .apply { permissions.addAll(newPermissions) }

        }.toSet()
        user.roles.addAll(roles)
    }

    fun createUser(username: String, password: String): User {
        val user = UserModelMapper.fromEntity(userDao.createUser(username, password))
        val role = rolesDao.createRole(username, "User role")
        rolePermissionDao.createPermissionForRoleId(role.id, "users:view:${role.id}")
        //TODO what else permissions ?
        userRolesDao.createRoleForUserId(user.id, role.id)
        fillUserRoles(user)
        return user
    }

    fun deleteUser(userId: Long) {
        userRolesDao.deleteRoleForUserId(userId)
        userDao.deleteUser(userId)
    }
}