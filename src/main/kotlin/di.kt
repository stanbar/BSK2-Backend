
import DatabaseTag.DB_PATH
import com.j256.ormlite.jdbc.JdbcConnectionSource
import data.domain.car.CarDao
import data.domain.car.CarDaoImpl
import data.domain.mechanic.MechanicDao
import data.domain.mechanic.MechanicDaoImpl
import data.domain.rent.RentDao
import data.domain.rent.RentDaoImpl
import data.domain.repair.RepairDao
import data.domain.repair.RepairDaoImpl
import data.domain.user.UserDao
import data.domain.user.UserDaoImpl
import data.rbac.role.RoleDao
import data.rbac.role.RoleDaoImpl
import data.rbac.rolepermission.RolePermissionDao
import data.rbac.rolepermission.RolePermissionDaoImpl
import data.rbac.subject.SubjectDao
import data.rbac.subject.SubjectDaoImpl
import data.rbac.subject_role.SubjectRolesDao
import data.rbac.subject_role.SubjectRolesDaoImpl
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.session.mgt.DefaultSessionManager
import org.apache.shiro.session.mgt.SessionManager
import org.kodein.di.Kodein
import org.kodein.di.generic.*
import service.MechanicService
import service.RoleService
import service.SubjectService
import service.UserService

object DatabaseTag{
    const val DB_PATH = "dbPath"

}
val dbModule = Kodein.Module {
    constant(DB_PATH) with "jdbc:sqlite:database.db"

    bind<JdbcConnectionSource>() with provider { JdbcConnectionSource(instance(DB_PATH)) }
}
val domainModule = Kodein.Module {
    bind<CarDao>() with singleton { CarDaoImpl(instance()) }
    bind<MechanicDao>() with singleton { MechanicDaoImpl(instance()) }
    bind<UserDao>() with singleton { UserDaoImpl(instance()) }
    bind<RentDao>() with singleton { RentDaoImpl(instance()) }
    bind<RepairDao>() with singleton { RepairDaoImpl(instance()) }
}
val rbacModule = Kodein.Module {
    bind<SubjectDao>() with singleton { SubjectDaoImpl(instance()) }
    bind<RoleDao>() with singleton { RoleDaoImpl(instance()) }
    bind<SubjectRolesDao>() with singleton { SubjectRolesDaoImpl(instance()) }
    bind<RolePermissionDao>() with singleton { RolePermissionDaoImpl(instance()) }

}
val shiroModule = Kodein.Module {
    bind<AuthorizingRealm>() with singleton { MyRealm(instance()) }
    bind<SessionManager>() with singleton { DefaultSessionManager() }
    bind<SecurityManager>() with singleton {
        DefaultSecurityManager(instance<AuthorizingRealm>()).apply {
            val sessionManager: SessionManager by kodein.instance()
            this.sessionManager = sessionManager
        }
    }
}

val kodein = Kodein {
    import(domainModule)
    import(rbacModule)
    import(shiroModule)
    import(dbModule)

    bind<MechanicService>() with singleton { MechanicService(kodein) }
    bind<UserService>() with singleton { UserService(kodein) }
    bind<SubjectService>() with singleton { SubjectService(kodein) }
    bind<RoleService>() with singleton { RoleService(kodein) }
}