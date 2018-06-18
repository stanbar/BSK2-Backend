package com.milbar.data.rbac.rolepermission

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class RolePermissionDaoImpl(connectionSource: ConnectionSource)
    : BaseDaoImpl<RolePermission, Void>(connectionSource, RolePermission::class.java), RolePermissionDao