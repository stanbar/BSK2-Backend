package data

import data.role.RoleDao
import data.rolepermission.RolePermissionDaoImpl
import data.user.UserDao
import data.userrole.UserRolesDao
import org.apache.shiro.crypto.hash.Sha256Hash
import java.sql.DriverManager


class Database {
    val DB_PATH = //":memory:"
     "mydatabase.db"

    fun makeConnection() = DriverManager.getConnection("jdbc:sqlite:$DB_PATH")

    fun bootstrap(userDao: UserDao, roleDao: RoleDao, rolePermissionDao: RolePermissionDaoImpl, userRolesDao: UserRolesDao) {
        userDao.recreate()
        roleDao.recreate()
        rolePermissionDao.recreate()
        userRolesDao.recreate()

        val userRole = roleDao.createRole("user", "The default role given to all users.")
        val readerRole = roleDao.createRole("reader", "Allows to view all database")
        val adminRole = roleDao.createRole("admin", "The administrator role only given to site admins")
        rolePermissionDao.createPermissionForRoleId(adminRole.id, "*")
        rolePermissionDao.createPermissionForRoleId(readerRole.id, "*:read")
        val adminUser = userDao.createUser(username = "admin", hashedPassword = Sha256Hash("admin").toHex())
        userRolesDao.createRoleForUserId(adminUser.id, adminRole.id)
    }

}