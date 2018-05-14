
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
        val connectionSourceDomain: ConnectionSource by kodein.instance()

        with(connectionSourceDomain) {
            TableUtils.createTableIfNotExists(this, Car::class.java)
            TableUtils.createTableIfNotExists(this, Mechanic::class.java)
            TableUtils.createTableIfNotExists(this, Rent::class.java)
            TableUtils.createTableIfNotExists(this, Repair::class.java)
            TableUtils.createTableIfNotExists(this, User::class.java)

            TableUtils.createTableIfNotExists(this, Role::class.java)
            TableUtils.createTableIfNotExists(this, RolePermission::class.java)
            TableUtils.createTableIfNotExists(this, Subject::class.java)
            TableUtils.createTableIfNotExists(this, SubjectRole::class.java)

        }


        //createDefaultRole();

    }
    fun createDefaultRole(){
        val roleService: RoleService by kodein.instance()
        roleService.createRole("carsReader",
                "The default role given to all users, it allows to view all cars",
                listOf("cars:view:*"))
    }
}


