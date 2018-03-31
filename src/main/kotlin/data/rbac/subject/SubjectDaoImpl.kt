package data.rbac.subject

import com.github.salomonbrys.kodein.Kodein
import data.Dao

class SubjectDaoImpl(kodein: Kodein) : SubjectDao, Dao(kodein) {
    override val tableName: String = "Subject"
    override val create: String = "create TABLE $tableName (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, hashedPassword TEXT NOT NULL)"


    override fun findSubjectById(subjectId: Long) = findSubjectBy(SubjectDao.Selector.ID.selector, subjectId)

    override fun findSubjectByName(name: String) = findSubjectBy(SubjectDao.Selector.NAME.selector, name)


    override fun <T> findSubjectBy(selector: String, value: T): SubjectEntity? {
        val sqlQuery = "SELECT * FROM $tableName WHERE $selector = ?"
        connect().use {
            it.query(sqlQuery) {
                setObject(1, value)
            }.use {
                it.executeQuery().use {
                    while (it.next()) {
                        val id = it.getLong("id")
                        val username = it.getString("name")
                        val hashedPassword = it.getString("hashedPassword")
                        return SubjectEntity(id, username, hashedPassword)
                    }
                }
                return null
            }
        }
    }

    override fun createSubject(name: String, hashedPassword: String): SubjectEntity {
        val id = execute("INSERT INTO $tableName (name, hashedPassword) VALUES (\"$name\", \"$hashedPassword\")")
        return SubjectEntity(id, name, hashedPassword)
    }


    override fun getAllSubject(): List<SubjectEntity> {
        val list = arrayListOf<SubjectEntity>()
        connect().query("SELECT * FROM $tableName ORDER BY name").use {
            val resultSet = it.executeQuery()
            resultSet.use {
                while (it.next()) {
                    val id = it.getLong("id")
                    val username = it.getString("name")
                    val hashedPassword = it.getString("hashedPassword")
                    list.add(SubjectEntity(id, username, hashedPassword))
                }
            }
        }
        return list
    }

    override fun deleteSubject(subjectId: Long) {
        execute("DELETE FROM $tableName WHERE id = $subjectId")
    }

}