package data.user

class UserEntity(val id: Long? = null,
                 val username: String,
                 val hashedPassword: String)