package data.rbac.subject


interface SubjectDao {
    enum class Selector(val selector: String) {
        ID("id"), NAME("name")
    }
    fun recreate()
    fun findSubjectById(subjectId: Long): SubjectEntity?
    fun findSubjectByName(name: String) : SubjectEntity?
    fun <T> findSubjectBy(selector: String, value: T): SubjectEntity?
    fun createSubject(name: String, hashedPassword:
    String): SubjectEntity
    fun getAllSubject(): List<SubjectEntity>
    fun deleteSubject(subjectId: Long)

}