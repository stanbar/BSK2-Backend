package data.rbac.role

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource


class RoleDaoImpl(connectionSource: ConnectionSource) : BaseDaoImpl<Role, Long>(connectionSource, Role::class.java), RoleDao
