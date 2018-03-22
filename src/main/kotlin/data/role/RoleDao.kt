package data.role

import data.Dao
import entrypoint.Database


class RoleDao(database: Database) :  Dao(database) {
    override val TABLE_NAME: String = "Role"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT NOT NULL)"


    fun getRole(roleId: Long): RoleEntity? {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun findRole(rolename: String): RoleEntity? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun createRole(name: String, description: String): RoleEntity {
        val id = execute("INSERT INTO Role(name, description) VALUES(\"$name\", \"$description\")")
        return RoleEntity(id, name, description)
    }


    fun getAllRoles(): List<RoleEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun deleteRole(roleId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}