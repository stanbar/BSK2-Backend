package service


import data.rbac.role.Role
import data.rbac.role.RoleDao
import data.rbac.rolepermission.RolePermission
import data.rbac.rolepermission.RolePermissionDao
import data.rbac.subject.Subject
import data.rbac.subject_role.SubjectRole
import data.rbac.subject_role.SubjectRolesDao
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class RoleService(override val kodein: Kodein) : KodeinAware, Service<Role, RoleDao>() {
    enum class Selector(val value: String) {
        ID("id"), NAME("name")
    }

    override val dao : RoleDao by instance()
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

    fun setDefaultRoleFor(subject: Subject) {

        val defaultRole = createRole(
                name = "subject_${subject.login}_${subject.id}",
                description = "Subject login: ${subject.login} id: ${subject.id} role",
                permissionsStrings = listOf("subjects:view:${subject.id}"))

        SubjectRole().apply {
            role = defaultRole
            this.subject = subject
        }.also { subjectRoleDao.create(it) }

    }

}