package data.user

import data.ModelMapper
import data.role.RolesDao
import model.User

class UserModelMapper(val rolesDao: RolesDao) : ModelMapper<UserEntity, User> {

    override fun fromEntity(entity: UserEntity): User {
        val roles = rolesDao.getRolesForRoleId(entity.id)
        return User(entity.id, entity.username, entity.password, roles)
    }

    override fun toEntity(user: User) = UserEntity(user.id, user.username, user.password)
}