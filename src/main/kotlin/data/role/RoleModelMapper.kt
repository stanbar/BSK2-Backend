package data.role

import data.ModelMapper
import model.Role

object RoleModelMapper : ModelMapper<RoleEntity, Role> {
    override fun fromEntity(entity: RoleEntity) = Role(entity.id, entity.name, entity.description)

    override fun toEntity(item: Role) = RoleEntity(item.id, item.name, item.description)

}