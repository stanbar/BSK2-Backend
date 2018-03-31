package data.rbac.subject_role

import com.github.salomonbrys.kodein.Kodein
import data.Dao

class SubjectRolesDaoImpl(kodein: Kodein) : SubjectRolesDao, Dao(kodein){


    override val tableName: String = "User_Roles"
    override val create: String = "create TABLE $tableName (subjectId INTEGER, roleId INTEGER, FOREIGN KEY(subjectId) REFERENCES Subject(id), FOREIGN KEY(roleId) REFERENCES Role(id) )"

    override fun createRoleForSubjectId(subjectId: Long, roleId: Long): Long {
        return execute("INSERT INTO $tableName(subjectId, roleId) VALUES($subjectId, $roleId)")
    }

    override fun getRolesForSubjectId(subjectId: Long): Set<SubjectRoleEntity> {
        val list = hashSetOf<SubjectRoleEntity>()
        connect().use {
            it.query("SELECT * FROM $tableName WHERE subjectId = ?"){
                setLong(1, subjectId)
            }.use {
                val resultSet = it.executeQuery()
                while (resultSet.next()) {
                    val roleId = resultSet.getLong("roleId")
                    list.add(SubjectRoleEntity(subjectId, roleId))
                }
                return list
            }
        }
    }

    override fun deleteWhereSubjectId(subjectId: Long) {
        execute("DELETE FROM $tableName WHERE subjectId = $subjectId")
    }

    override fun deleteWhereRoleId(roleId: Long) {
        execute("DELETE FROM $tableName WHERE roleId = $roleId")
    }
}