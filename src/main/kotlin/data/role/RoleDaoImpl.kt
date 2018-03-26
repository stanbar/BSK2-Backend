package data.role

import com.github.salomonbrys.kodein.Kodein
import data.Dao


class RoleDaoImpl(kodein: Kodein) : RoleDao, Dao(kodein) {
    override val TABLE_NAME: String = "Role"
    override val CREATE: String = "CREATE TABLE $TABLE_NAME (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, description TEXT NOT NULL)"

    override fun findRoleById(roleId: Long) = findRoleBy(RoleDao.Selector.ID.selector, roleId)

    override fun findRoleByName(rolename: String) = findRoleBy(RoleDao.Selector.NAME.selector, rolename)

    override fun findRoleBy(selector: String, value: Any): RoleEntity? {
        connect().query("SELECT * FROM $TABLE_NAME WHERE $selector = ?") {
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

    override fun createRole(name: String, description: String): RoleEntity {
        val id = execute("INSERT INTO Role(name, description) VALUES(\"$name\", \"$description\")")
        return RoleEntity(id, name, description)
    }


    override fun getAllRoles(): List<RoleEntity> {
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

    override fun deleteRole(roleId: Long) {
        execute("DELETE FROM $TABLE_NAME WHERE id = $roleId")
    }
}