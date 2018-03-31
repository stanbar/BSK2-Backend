package data.rbac.subject

object SubjectModelMapper{

    fun fromEntity(entity: SubjectEntity): Subject {
        return Subject(entity.id, entity.name, entity.hashedPassword)
    }

    fun toEntity(item: Subject) = SubjectEntity(item.id, item.name, item.password)
}