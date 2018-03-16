package service

import data.role.RoleDao
import data.role.RoleModelMapper
import data.user.UserDao
import data.user.UserModelMapper
import data.userrole.UserRolesDao
import model.User

class UserService(val userDao: UserDao, val rolesDao: RoleDao, val userRolesDao: UserRolesDao) {

    fun getAllUsers(): List<User> {
        return userDao.getAllUsers().map {
            val user = UserModelMapper.fromEntity(it)
            val userRoles = userRolesDao.getRolesForUserId(user.id)
            val roles = userRoles.map {
                val roleEntity = rolesDao.getRole(it.roleId)
                        ?: throw NullPointerException("Could not find role entity of id ${it.roleId}")
                RoleModelMapper.fromEntity(roleEntity)
            }.toSet()
            user.roles.addAll(roles)
            user
        }
    }

    fun getUser(userId: Long): User? {
        val userEntity = userDao.getUser(userId) ?: return null
        val user = UserModelMapper.fromEntity(userEntity)
        val userRoles = userRolesDao.getRolesForUserId(user.id)
        val roles = userRoles.map {
            val roleEntity = rolesDao.getRole(it.roleId)
                    ?: throw NullPointerException("Could not find role entity of id ${it.roleId}")
            RoleModelMapper.fromEntity(roleEntity)
        }.toSet()
        user.roles.addAll(roles)
        return user
    }

    fun findUser(username: String): User? {
        val userEntity = userDao.findUser(username) ?: return null
        val user = UserModelMapper.fromEntity(userEntity)
        val userRoles = userRolesDao.getRolesForUserId(user.id)
        val roles = userRoles.map {
            val roleEntity = rolesDao.getRole(it.roleId)
                    ?: throw NullPointerException("Could not find role entity of id ${it.roleId}")
            RoleModelMapper.fromEntity(roleEntity)
        }.toSet()
        user.roles.addAll(roles)
        return user
    }

    fun createUser(username: String, password: String) = userDao.createUser(username, password)

    fun deleteUser(userId: Long) = userDao.deleteUser(userId)
}