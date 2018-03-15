package data.role

import data.DaoImpl
import entrypoint.Database

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

    override fun getRole(roleId: Long): RoleEntity? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findRole(rolename: String): RoleEntity? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createRole(role: RoleEntity): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllRoles(): List<RoleEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteRole(roleId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}