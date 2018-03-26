package data.user


interface UserDao {
    enum class Selector(val selector: String) {
        ID("id"), USERNAME("username")
    }
    fun recreate()
    fun findUserById(userId: Long): UserEntity?
    fun findUserByName(userName: String) : UserEntity?
    fun <T> findUserBy(selector: String, value: T): UserEntity?
    fun createUser(username: String, hashedPassword: String): UserEntity
    fun getAllUsers(): List<UserEntity>
    fun deleteUser(userId: Long)

}