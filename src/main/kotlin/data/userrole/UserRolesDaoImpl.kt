package data.userrole

import com.github.salomonbrys.kodein.Kodein
import data.Dao

class UserRolesDaoImpl(kodein: Kodein) : UserRolesDao, Dao(kodein){
    override val TABLE_NAME: String = "User_Roles"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (userId INTEGER, roleId INTEGER, FOREIGN KEY(userId) REFERENCES User(id), FOREIGN KEY(roleId) REFERENCES Role(id) )"

    override fun createRoleForUserId(userId: Long, roleId: Long): Long {
        return execute("INSERT INTO $TABLE_NAME(userId, roleId) VALUES($userId, $roleId)")
    }

    override fun getRolesForUserId(userId: Long): Set<UserRoleEntity> {
        val list = hashSetOf<UserRoleEntity>()
        connect().use {
            it.query("SELECT * FROM $TABLE_NAME WHERE userId = ?"){
                setLong(1, userId)
            }.use {
                val resultSet = it.executeQuery()
                while (resultSet.next()) {
                    val roleId = resultSet.getLong("roleId")
                    list.add(UserRoleEntity(userId, roleId))
                }
                return list
            }
        }
    }

    override fun deleteRoleForUserId(userId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE userId = $userId")
    }

}