package data.rbac.role


interface RoleDao {
    enum class Selector(val selector: String) {
        ID("id"), NAME("name")
    }
    fun recreate()

    fun findRoleById(roleId: Long): RoleEntity?

    fun findRoleByName(rolename: String): RoleEntity?

    fun findRoleBy(selector: String, value: Any): RoleEntity?

    fun createRole(name: String, description: String): RoleEntity

    fun getAllRoles(): List<RoleEntity>

    fun deleteRole(roleId: Long)
}