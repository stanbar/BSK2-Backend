package data.rbac.rolepermission

object RolePermissionModelMapper {
    fun fromEntity(entity: RolePermissionEntity): String {
        return entity.permission
    }

    fun toEntity(item: String, roleId: Long): RolePermissionEntity {
        return RolePermissionEntity(item, roleId)
    }
}