package data.user

import com.github.salomonbrys.kodein.Kodein
import data.Dao

class UserDaoImpl(kodein: Kodein) : UserDao, Dao(kodein) {
    override val TABLE_NAME: String = "User"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, hashedPassword TEXT NOT NULL)"


    override fun findUserById(userId: Long) = findUserBy(UserDao.Selector.ID.selector, userId)

    override fun findUserByName(userName: String) = findUserBy(UserDao.Selector.USERNAME.selector, userName)


    override fun <T> findUserBy(selector: String, value: T): UserEntity? {
        val sqlQuery = "SELECT * FROM $TABLE_NAME WHERE $selector = ?"
        connect().use {
            it.query(sqlQuery) {
                setObject(1, value)
            }.use {
                it.executeQuery().use {
                    while (it.next()) {
                        val id = it.getLong("id")
                        val username = it.getString("username")
                        val hashedPassword = it.getString("hashedPassword")
                        return UserEntity(id, username, hashedPassword)
                    }
                }
                return null
            }
        }
    }

    override fun createUser(username: String, hashedPassword: String): UserEntity {
        val id = execute("INSERT INTO $TABLE_NAME (username, hashedPassword) VALUES (\"$username\", \"$hashedPassword\")")
        return UserEntity(id, username, hashedPassword)
    }


    override fun getAllUsers(): List<UserEntity> {
        val list = arrayListOf<UserEntity>()
        connect().query("SELECT * FROM $TABLE_NAME ORDER BY username").use {
            val resultSet = it.executeQuery()
            resultSet.use {
                while (it.next()) {
                    val id = it.getLong("id")
                    val username = it.getString("username")
                    val hashedPassword = it.getString("hashedPassword")
                    list.add(UserEntity(id, username, hashedPassword))
                }
            }
        }
        return list
    }

    override fun deleteUser(userId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE id = $userId")
    }

}