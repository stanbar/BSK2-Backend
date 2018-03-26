package data.userrole


interface UserRolesDao {
    fun recreate()
    fun createRoleForUserId(userId: Long, roleId: Long): Long
    fun getRolesForUserId(userId: Long): Set<UserRoleEntity>
    fun deleteRoleForUserId(userId: Long)
}