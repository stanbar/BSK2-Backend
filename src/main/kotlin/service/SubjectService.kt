package service

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import data.rbac.role.RoleDao
import data.rbac.role.RoleModelMapper
import data.rbac.rolepermission.RolePermissionDao
import data.rbac.subject.SubjectDao
import data.rbac.subject.SubjectModelMapper
import data.rbac.subject_role.SubjectRolesDao
import data.rbac.subject.Subject

class SubjectService(override val kodein: Kodein) : KodeinAware {
    private val subjectDao: SubjectDao = instance()
    private val rolesDao: RoleDao = instance()
    private val subjectRolesDao: SubjectRolesDao = instance()
    private val rolePermissionDao: RolePermissionDao = instance()

    fun findSubjectById(subjectId: Long) = findSubjectBy(SubjectDao.Selector.ID, subjectId)

    fun findSubjectByName(name: String) = findSubjectBy(SubjectDao.Selector.NAME, name)

    private fun findSubjectBy(selector: SubjectDao.Selector, value: Any): Subject? {
        val subjectEntity = subjectDao.findSubjectBy(selector.selector, value) ?: return null
        val subject = SubjectModelMapper.fromEntity(subjectEntity)
        fillSubjectRoles(subject)
        return subject
    }

    fun getAllSubjects(): List<Subject> {
        return subjectDao.getAllSubject().map {
            val subject = SubjectModelMapper.fromEntity(it)
            fillSubjectRoles(subject)
            subject
        }
    }
    fun createSubject(name: String, password: String): Subject {
        val subject = SubjectModelMapper.fromEntity(subjectDao.createSubject(name, password))
        val role = rolesDao.createRole("subject_${subject.id}", "Subject ${subject.id} role")
        rolePermissionDao.createPermissionForRoleId(role.id, "subjects:view:${role.id}")
        //TODO what else permissions ?
        subjectRolesDao.createRoleForSubjectId(subject.id, role.id)
        fillSubjectRoles(subject)
        return subject
    }
    private fun fillSubjectRoles(subject: Subject) {
        val subjectRoles = subjectRolesDao.getRolesForSubjectId(subject.id)
        val roles = subjectRoles.map {
            val roleEntity = rolesDao.findRoleById(it.roleId)
                    ?: throw NullPointerException("Could not find role entity of id ${it.roleId}")

            val newPermissions = rolePermissionDao.getPermissionsForRoleId(roleEntity.id)
                    .map { it.permission }

            RoleModelMapper.fromEntity(roleEntity)
                    .apply { permissions.addAll(newPermissions) }

        }.toSet()
        subject.roles.addAll(roles)
    }



    fun deleteSubject(subjectId: Long) {
        subjectRolesDao.deleteWhereSubjectId(subjectId)
        subjectDao.deleteSubject(subjectId)
    }
}