package data.rbac.rolepermission

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class RolePermissionDaoImpl(connectionSource: ConnectionSource)
    : BaseDaoImpl<RolePermission, Void>(connectionSource, RolePermission::class.java), RolePermissionDao {
    override fun getPermissionsForRoleId(roleId: Long): Set<RolePermission> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createPermissionForRoleId(roleId: Long, permission: String): RolePermission {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deletePermissionForRoleId(roleId: Long, permission: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteAllPermissionsForRoleId(roleId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}