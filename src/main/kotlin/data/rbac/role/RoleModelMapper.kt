package data.rbac.role

object RoleModelMapper{
    fun fromEntity(entity: RoleEntity) = Role(entity.id, entity.name, entity.description)

    fun toEntity(item: Role) = RoleEntity(item.id, item.name, item.description)

}