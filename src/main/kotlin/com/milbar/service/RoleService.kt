package com.milbar.service


import com.milbar.Permission
import com.milbar.data.rbac.role.Role
import com.milbar.data.rbac.role.RoleDao
import com.milbar.data.rbac.rolepermission.RolePermission
import com.milbar.data.rbac.rolepermission.RolePermissionDao
import com.milbar.data.rbac.subject.Subject
import com.milbar.data.rbac.subject_role.SubjectRole
import com.milbar.data.rbac.subject_role.SubjectRolesDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class RoleService(override val kodein: Kodein) : KodeinAware, Service<Role, RoleDao>() {
    enum class Selector(val value: String) {
        ID("id"), NAME("name")
    }

    override val dao: RoleDao by instance()
    private val rolePermissionDao: RolePermissionDao by instance()
    private val subjectRoleDao: SubjectRolesDao by instance()

    fun createRole(name: String, description: String, permissionsStrings: List<String>): Role {
        val role = Role().apply {
            this.name = name
            this.description = description
        }.also { dao.create(it) }

        permissionsStrings.map {
            RolePermission().apply { permission = it; this.role = role }
        }.forEach {
            rolePermissionDao.create(it)
        }

        dao.refresh(role)
        return role
    }

    fun setDefaultRoleFor(subject: Subject): Role {
        val defaultRole = createRole(
                name = "subject_${subject.login}_${subject.id}",
                description = "Default role for each subject, having access to read his data, read all cars, and create rent",
                permissionsStrings = listOf(
                        Permission.from(Permission.Domain.SUBJECT, Permission.Action.READ, subject.id),
                        Permission.from(Permission.Domain.CAR, Permission.Action.READ),
                        Permission.from(Permission.Domain.RENT, Permission.Action.CREATE)
                )
        )
        addRoleToSubject(defaultRole, subject)
        return defaultRole

    }

    fun addRoleToSubject(role: Role, subject: Subject) {
        SubjectRole().apply {
            this.role = role
            this.subject = subject
        }.also {
            subjectRoleDao.create(it)
        }

    }

    fun createPermissionForRole(permission: String, role: Role): RolePermission {
        return RolePermission().apply {
            this.permission = permission
            this.role = role
        }.apply { rolePermissionDao.create(this) }
    }

}