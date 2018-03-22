package data.user

import data.Dao
import data.Database


class UserDao(database: Database) : Dao(database) {
    override val TABLE_NAME: String = "User"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, hashedPassword TEXT NOT NULL)"

    enum class Selector(val selector: String) {
        ID("id"), USERNAME("username")
    }

    fun findUserById(userId: Long) = findUserBy(Selector.ID, userId)

    fun findUserByName(userName: String) = findUserBy(Selector.USERNAME, userName)


    fun <T> findUserBy(selector: Selector, value: T): UserEntity? {
        val sqlQuery = "SELECT * FROM $TABLE_NAME WHERE ${selector.selector} = ?"
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

    fun createUser(username: String, hashedPassword: String): UserEntity {
        val id = execute("INSERT INTO $TABLE_NAME (username, hashedPassword) VALUES (\"$username\", \"$hashedPassword\")")
        return UserEntity(id, username, hashedPassword)
    }


    fun getAllUsers(): List<UserEntity> {
        val list = arrayListOf<UserEntity>()
        connect().query("SELECT * FROM $TABLE_NAME ORDER BY username").use{
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

    fun deleteUser(userId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE id = $userId")
    }

}