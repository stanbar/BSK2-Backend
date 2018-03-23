package model

open class User(val id: Long,
                val username: String,
                val password: String,
                val roles: MutableSet<Role> = hashSetOf())