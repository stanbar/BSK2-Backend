
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import data.role.RoleDao
import data.role.RoleDaoImpl
import data.rolepermission.RolePermissionDao
import data.rolepermission.RolePermissionDaoImpl
import data.user.UserDao
import data.userrole.UserRolesDao
import org.apache.shiro.crypto.hash.Sha256Hash

object Utils{
    fun bootstrapDatabase(kodein: Kodein){
        val userDao: UserDao = kodein.instance()
        val roleDao: RoleDao = kodein.instance()
        val rolePermissionDao: RolePermissionDao = kodein.instance()
        val userRolesDao: UserRolesDao = kodein.instance()

        userDao.recreate()
        roleDao.recreate()
        rolePermissionDao.recreate()
        userRolesDao.recreate()

        roleDao.createRole("user", "The default role given to all users.")
        val readerRole = roleDao.createRole("reader", "Allows to view all database")
        val adminRole = roleDao.createRole("admin", "The administrator role only given to site admins")
        rolePermissionDao.createPermissionForRoleId(adminRole.id, "*")
        rolePermissionDao.createPermissionForRoleId(readerRole.id, "*:read")
        val adminUser = userDao.createUser(username = "admin", hashedPassword = Sha256Hash("admin").toHex())
        userRolesDao.createRoleForUserId(adminUser.id, adminRole.id)
    }

}