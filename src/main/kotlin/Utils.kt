
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import com.j256.ormlite.table.TableUtils
import data.domain.car.Car
import data.domain.mechanic.Mechanic
import data.domain.rent.Rent
import data.domain.repair.Repair
import data.domain.user.User
import data.rbac.role.RoleDao
import data.rbac.rolepermission.RolePermissionDao
import data.rbac.subject.SubjectDao
import data.rbac.subject_role.SubjectRolesDao
import org.apache.shiro.crypto.hash.Sha256Hash

object Utils{
    fun bootstrapDatabase(kodein: Kodein){
        val subjectDao: SubjectDao = kodein.instance()
        val roleDao: RoleDao = kodein.instance()
        val rolePermissionDao: RolePermissionDao = kodein.instance()
        val subjectRolesDao: SubjectRolesDao = kodein.instance()

        subjectDao.recreate()
        roleDao.recreate()
        rolePermissionDao.recreate()
        subjectRolesDao.recreate()

        TableUtils.createTableIfNotExists(kodein.instance(), Car::class.java)
        TableUtils.createTableIfNotExists(kodein.instance(), Mechanic::class.java)
        TableUtils.createTableIfNotExists(kodein.instance(), Rent::class.java)
        TableUtils.createTableIfNotExists(kodein.instance(), Repair::class.java)
        TableUtils.createTableIfNotExists(kodein.instance(), User::class.java)


        roleDao.createRole("subject", "The default role given to all users.")
        val readerRole = roleDao.createRole("reader", "Allows to view all database")
        val adminRole = roleDao.createRole("admin", "The administrator role only given to site admins")
        rolePermissionDao.createPermissionForRoleId(adminRole.id, "*")
        rolePermissionDao.createPermissionForRoleId(readerRole.id, "*:read")
        val adminSubject = subjectDao.createSubject(name = "admin", hashedPassword = Sha256Hash("admin").toHex())
        subjectRolesDao.createRoleForSubjectId(adminSubject.id, adminRole.id)
    }

}