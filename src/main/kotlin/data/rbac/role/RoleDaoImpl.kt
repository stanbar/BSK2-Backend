package data.rbac.role

import com.github.salomonbrys.kodein.Kodein
import data.Dao


class RoleDaoImpl(kodein: Kodein) : RoleDao, Dao(kodein) {
    override val tableName: String = "Role"
    override val create: String = "create TABLE $tableName (id INTEGER PRIMARY KEY AUTOINCREMENT, name T" +
            " description TEXT NOT NULL)"

    override fun findRoleById(roleId: Long) = findRoleBy(RoleDao.Selector.ID.selector, roleId)

    override fun findRoleByName(rolename: String) = findRoleBy(RoleDao.Selector.NAME.selector, rolename)

    override fun findRoleBy(selector: String, value: Any): RoleEntity? {
        connect().query("SELECT * FROM $tableName WHERE $selector = ?") {
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

        connect().query("SELECT * FROM $tableName ORDER BY name").use {
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
        execute("DELETE FROM $tableName WHERE id = $roleId")
    }
}