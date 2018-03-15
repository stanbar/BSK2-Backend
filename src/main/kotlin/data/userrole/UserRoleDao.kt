package data.userrole

import data.DaoImpl
import entrypoint.Database

interface UserRoleDao {

    fun createRoleForUserId(userId: Long, roleId: Long): Long

    fun getRolesForUserId(userId: Long): Set<UserRoleEntity>

    fun deleteRoleForUserId(userId: Long)

}

class UserRoleDaoImpl(database: Database) : UserRoleDao, DaoImpl(database) {
    companion object {
        const val SCHEMA = "CREATE TABLE User_Roles (userId INTEGER FOREIGN KEY REFERENCES User NOT NULL, roleId INTEGER FOREIGN KEY  REFERENCES Role NOT NULL)"
    }

    override fun createRoleForUserId(userId: Long, roleId: Long): Long {
        return execute("INSERT INTO User_Roles VALUES ($userId,$roleId)")
    }


    override fun getRolesForUserId(userId: Long): Set<UserRoleEntity> {
        val list = hashSetOf<UserRoleEntity>()
        val resultSet = select("SELECT * FROM User_Roles")
        while (resultSet.next()) {
            val userId = resultSet.getLong("userId")
            val roleId = resultSet.getLong("roleId")
            list.add(UserRoleEntity(userId, roleId))
        }
        return list
    }

    override fun deleteRoleForUserId(userId: Long) {
        execute("DELETE FROM User_Roles WHERE userId = $userId")
    }

}