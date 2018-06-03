package com.milbar.data.rbac.subject_role

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.milbar.data.rbac.role.Role
import com.milbar.data.rbac.subject.Subject

@DatabaseTable(tableName = "Subject_Role", daoClass = SubjectRolesDaoImpl::class)
class SubjectRole {
    @Transient
    @DatabaseField(foreign = true, foreignAutoCreate = true)
    lateinit var subject: Subject

    @DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    lateinit var role: Role
}
