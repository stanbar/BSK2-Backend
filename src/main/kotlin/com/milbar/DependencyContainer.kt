package com.milbar
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.milbar.DatabaseTag.DB_PATH
import com.milbar.data.domain.car.CarDao
import com.milbar.data.domain.car.CarDaoImpl
import com.milbar.data.domain.mechanic.MechanicDao
import com.milbar.data.domain.mechanic.MechanicDaoImpl
import com.milbar.data.domain.rent.RentDao
import com.milbar.data.domain.rent.RentDaoImpl
import com.milbar.data.domain.repair.RepairDao
import com.milbar.data.domain.repair.RepairDaoImpl
import com.milbar.data.domain.user.UserDao
import com.milbar.data.domain.user.UserDaoImpl
import com.milbar.data.rbac.role.RoleDao
import com.milbar.data.rbac.role.RoleDaoImpl
import com.milbar.data.rbac.rolepermission.RolePermissionDao
import com.milbar.data.rbac.rolepermission.RolePermissionDaoImpl
import com.milbar.data.rbac.subject.SubjectDao
import com.milbar.data.rbac.subject.SubjectDaoImpl
import com.milbar.data.rbac.subject_role.SubjectRolesDao
import com.milbar.data.rbac.subject_role.SubjectRolesDaoImpl
import com.milbar.service.*
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.session.mgt.DefaultSessionManager
import org.apache.shiro.session.mgt.SessionManager
import org.kodein.di.Kodein
import org.kodein.di.generic.*

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
    bind<SessionManager>() with singleton { DefaultSessionManager().apply {
        globalSessionTimeout = 36000000
    } }
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
    bind<CarService>() with singleton { CarService(kodein) }
    bind<RentService>() with singleton { RentService(kodein) }
    bind<RepairService>() with singleton { RepairService(kodein) }
}