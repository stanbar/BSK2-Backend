package data.rolepermission

import data.DaoImpl
import entrypoint.Database

interface RolePermissionDao {
    fun getPermisstionsForRoleId(roleId : Long) : Set<RolePermissionEntity>
    fun createPermissionForRoleId(roleId : Long, role : RolePermissionEntity) : Int
    fun deletePermissionForRoleId(roleId : Long, role : RolePermissionEntity)
}

class RolePermissionDaoImpl(database: Database) : RolePermissionDao, DaoImpl(database) {
    companion object {
        const val SCHEMA = "CREATE TABLE Role_Permissions (roleId INTEGER FOREIGN KEY REFERENCES Role NOT NULL, permission VARCHAR(255) NOT NULL)"
    }
    override fun getPermisstionsForRoleId(roleId: Long): Set<RolePermissionEntity> {
        val list = hashSetOf<RolePermissionEntity>()
        val resultSet = select("SELECT * FROM Role_Permissions WHERE roleId = $roleId")
        while (resultSet.next()){
            val roleId = resultSet.getLong("roleId")
            val permission = resultSet.getString("permission")
            list.add(RolePermissionEntity(roleId,permission))
        }
        return list
    }

    override fun createPermissionForRoleId(roleId: Long, role: RolePermissionEntity): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deletePermissionForRoleId(roleId: Long, role: RolePermissionEntity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}