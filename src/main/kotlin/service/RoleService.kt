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

class RoleService(override val kodein: Kodein) : KodeinAware {
    private val rolesDao: RoleDao by instance()
    private val rolePermissionDao: RolePermissionDao by instance()
    private val subjectRoleDao: SubjectRolesDao by instance()

    fun getAllRoles(): List<Role> = rolesDao.queryForAll()

    fun findRoleById(id: Long): Role? = rolesDao.queryForId(id)

    fun findRoleByName(name: String) = rolesDao.queryForEq("name", name)


    fun createRole(name: String, description: String, permissionsStrings: List<String>): Role {
        val role = Role().apply {
            this.name = name
            this.description = description
        }.also { rolesDao.create(it) }

        permissionsStrings.map {
            RolePermission().apply { permission = it; this.role = role }
        }.forEach {
            rolePermissionDao.create(it)
        }

        rolesDao.refresh(role)
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