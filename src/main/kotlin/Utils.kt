
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import data.domain.car.Car
import data.domain.mechanic.Mechanic
import data.domain.rent.Rent
import data.domain.repair.Repair
import data.domain.user.User
import data.rbac.role.Role
import data.rbac.rolepermission.RolePermission
import data.rbac.subject.Subject
import data.rbac.subject_role.SubjectRole
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import service.RoleService

object Utils {
    fun bootstrapDatabase(kodein: Kodein) {
        val roleService: RoleService by kodein.instance()
        val connectionSource : ConnectionSource by kodein.instance()
        TableUtils.createTableIfNotExists(connectionSource, Car::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Mechanic::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Rent::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Repair::class.java)
        TableUtils.createTableIfNotExists(connectionSource, User::class.java)

        TableUtils.createTableIfNotExists(connectionSource, Role::class.java)
        TableUtils.createTableIfNotExists(connectionSource, RolePermission::class.java)
        TableUtils.createTableIfNotExists(connectionSource, Subject::class.java)
        TableUtils.createTableIfNotExists(connectionSource, SubjectRole::class.java)

        roleService.createRole("carViewer",
                "The default role given to all users, it allows to view all cars",
                listOf("cars:view:*"))

    }

}