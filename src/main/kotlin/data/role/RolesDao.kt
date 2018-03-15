package data.role

import data.DaoImpl
import entrypoint.Database
import model.Role

interface RolesDao {

    fun getRole(roleId: Long): RoleEntity?

    fun findRole(rolename: String): RoleEntity?

    fun createRole(role: RoleEntity) : Long

    fun getAllRoles(): List<RoleEntity>

    fun deleteRole(roleId: Long) 
    
}

class RolesDaoImpl(database: Database) : RolesDao, DaoImpl(database){

    companion object {
        const val SCHEMA = "CREATE TABLE Role (id INTEGER PRIMARY KEY, name VARCHAR(100) NOT NULL, description VARCHAR(255) NOT NULL)"
    }

    fun getRolesForRoleId(userId: Long): Set<Role> {

    }
}