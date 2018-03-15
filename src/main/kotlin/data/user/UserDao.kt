package data.user

import data.DaoImpl
import entrypoint.Database
import org.apache.shiro.crypto.hash.Sha256Hash


interface UserDao {
    fun getUser(userId: Long): UserEntity?

    fun findUser(username: String): UserEntity?

    fun createUser(user: UserEntity) : Long

    fun getAllUsers(): List<UserEntity>

    fun deleteUser(userId: Long)

}

class UserDAOImpl(database: Database) : UserDao, DaoImpl(database) {
    companion object {
        const val SCHEMA = "CREATE TABLE User (id INTEGER PRIMARY KEY, username VARCHAR(100) NOT NULL, hashedPassword VARCHAR(255) NOT NULL)"
    }

    init {
        database.makeConnection().use {
            it.createStatement().use {
                it.executeUpdate(SCHEMA)

                insertRole("user", "The default rolepermission given to all users.")
                val adminRoleId = insertRole("admin", "The administrator rolepermission only given to site admins")
                insertRolePermissions(adminRoleId, "user:*")
                val adminId = createUser(UserEntity(username = "admin", hashedPassword = Sha256Hash("admin").toHex()))
                insertUserRoles(adminId, adminRoleId)
            }

        }

    }


    private fun insertUserRoles(userId: Long, roleId: Long) = execute("INSERT INTO User_Roles VALUES ($userId,$roleId)")

    private fun insertRole(name: String, description: String) = execute("INSERT INTO Role VALUES ($name,$description)")


    private fun insertRolePermissions(roleId: Long, permissions: String) = execute("INSERT INTO Role_Permissions VALUES ($roleId,$permissions)")


    override fun getUser(userId: Long): UserEntity? {
        val resultSet = select("SELECT * FROM User WHERE id = $userId")
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

    override fun findUser(username: String): UserEntity? {
        val resultSet = select("SELECT * FROM User WHERE username = $username")
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

    override fun createUser(user: UserEntity) =
            execute("INSERT INTO User VALUES (${user.username},${user.hashedPassword})")


    override fun getAllUsers(): List<UserEntity> {
        val list = arrayListOf<UserEntity>()
        val resultSet = select("SELECT * FROM User ORDER BY username")
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

    override fun deleteUser(userId: Long) {
        execute("DELETE FROM User WHERE id = $userId")
    }

}