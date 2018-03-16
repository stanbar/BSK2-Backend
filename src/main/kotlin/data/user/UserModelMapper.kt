package data.user

import data.ModelMapper
import model.User

object UserModelMapper : ModelMapper<UserEntity, User> {

    override fun fromEntity(entity: UserEntity): User {
        return User(entity.id, entity.username, entity.hashedPassword)
    }

    override fun toEntity(item: User) = UserEntity(item.id, item.username, item.password)
}