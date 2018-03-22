package data.role

import data.Dao
import data.Database


class RoleDao(database: Database) : Dao(database) {
    override val TABLE_NAME: String = "Role"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT NOT NULL)"

    enum class Selector(val selector: String) {
        ID("id"), NAME("name")
    }

    fun findRoleById(roleId: Long) = findRoleBy(Selector.ID, roleId)

    fun findRoleByName(rolename: String) = findRoleBy(Selector.NAME, rolename)

    fun findRoleBy(selector: Selector, value: Any): RoleEntity? {
        connect().query("SELECT * FROM $TABLE_NAME WHERE ${selector.selector} = ?") {
            setObject(1, value)
        }.use {
            val resultSet = it.executeQuery()
            resultSet.use {
                while (it.next()) {
                    val id = it.getLong("id")
                    val name = it.getString("name")
                    val description = it.getString("description")
                    return RoleEntity(id, name, description)
                }
            }
        }
        return null
    }

    fun createRole(name: String, description: String): RoleEntity {
        val id = execute("INSERT INTO Role(name, description) VALUES(\"$name\", \"$description\")")
        return RoleEntity(id, name, description)
    }


    fun getAllRoles(): List<RoleEntity> {
        val list = arrayListOf<RoleEntity>()

        connect().query("SELECT * FROM $TABLE_NAME ORDER BY name").use {
            val resultSet = it.executeQuery()
            resultSet.use {
                while (it.next()) {
                    val id = it.getLong("id")
                    val name = it.getString("name")
                    val description = it.getString("description")
                    list.add(RoleEntity(id, name, description))
                }
            }
        }
        return list
    }

    fun deleteRole(roleId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE id = $roleId")
    }


}