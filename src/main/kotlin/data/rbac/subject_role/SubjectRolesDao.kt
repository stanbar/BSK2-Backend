package data.rbac.subject_role


interface SubjectRolesDao {
    fun recreate()
    fun createRoleForSubjectId(subjectId: Long, roleId: Long): Long
    fun getRolesForSubjectId(subjectId: Long): Set<SubjectRoleEntity>
    fun deleteWhereSubjectId(subjectId: Long)
    fun deleteWhereRoleId(roleId: Long)
}