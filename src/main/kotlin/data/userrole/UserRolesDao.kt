package data.userrole

import data.Dao
import entrypoint.Database


class UserRolesDao(database: Database) : Dao(database) {
    override val TABLE_NAME: String
        = "User_Roles"
    override val CREATE: String
        = "CREATE TABLE $TABLE_NAME (userId INTEGER FOREIGN KEY REFERENCES User NOT NULL, roleId INTEGER FOREIGN KEY  REFERENCES Role NOT NULL)"


    fun createRoleForUserId(userId: Long, roleId: Long): Long {
        return execute("INSERT INTO $TABLE_NAME VALUES ($userId,$roleId)")
    }

    fun getRolesForUserId(userId: Long): Set<UserRoleEntity> {
        val list = hashSetOf<UserRoleEntity>()
        val resultSet = select("SELECT * FROM $TABLE_NAME")
        while (resultSet.next()) {
            val userId = resultSet.getLong("userId")
            val roleId = resultSet.getLong("roleId")
            list.add(UserRoleEntity(userId, roleId))
        }
        return list
    }

    fun deleteRoleForUserId(userId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE userId = $userId")
    }

}