package data.user

import data.Dao
import entrypoint.Database





class UserDao(database: Database) : Dao(database) {
    override val TABLE_NAME: String = "User"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY, username VARCHAR(100) NOT NULL, hashedPassword VARCHAR(255) NOT NULL)"

    fun getUser(userId: Long): UserEntity? {
        val resultSet = select("SELECT * FROM $TABLE_NAME WHERE id = $userId")
        resultSet.use {
            while (it.next()) {
                val id = it.getLong("id")
                val username = it.getString("username")
                val hashedPassword = it.getString("hashedPassword")
                return UserEntity(id, username, hashedPassword)
            }
        }
        return null
    }

    fun findUser(username: String): UserEntity? {
        val resultSet = select("SELECT * FROM $TABLE_NAME WHERE username = $username")
        //TODO Protect from SQL Injection
        resultSet.use {
            while (it.next()) {
                val id = it.getLong("id")
                val hashedPassword = it.getString("hashedPassword")
                return UserEntity(id, username, hashedPassword)
            }
        }
        return null
    }

    fun createUser(username: String, hashedPassword: String): UserEntity {
        val id = execute("INSERT INTO $TABLE_NAME VALUES ($username,$hashedPassword)")
        return UserEntity(id, username, hashedPassword)
    }


    fun getAllUsers(): List<UserEntity> {
        val list = arrayListOf<UserEntity>()
        val resultSet = select("SELECT * FROM $TABLE_NAME ORDER BY username")
        resultSet.use {
            while (it.next()) {
                val id = it.getLong("id")
                val username = it.getString("username")
                val hashedPassword = it.getString("hashedPassword")
                list.add(UserEntity(id, username, hashedPassword))
            }
        }
        return list
    }

    fun deleteUser(userId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE id = $userId")
    }

}