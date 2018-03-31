
import com.github.salomonbrys.kodein.*
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.stasbar.Logger
import data.Database
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
import entrypoint.MyRealm
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.*
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.session.mgt.DefaultSessionManager
import org.apache.shiro.session.mgt.SessionManager
import org.apache.shiro.subject.Subject
import service.RoleService
import service.SubjectService

class IdPrincipal(val id: Long) : Principal
class MySession(val id: String)

/**
 * Dependency Injection Container
 */
val kodein = Kodein {
    constant("dbPath") with "jdbc:sqlite:mydatabase.db"
    bind<Database>() with singleton { Database(kodein) }
    bind<JdbcConnectionSource>() with singleton { JdbcConnectionSource(instance("dbPath")) }
    bind<SubjectDao>() with singleton { SubjectDaoImpl(kodein) }
    bind<RoleDao>() with singleton { RoleDaoImpl(kodein) }
    bind<SubjectRolesDao>() with singleton { SubjectRolesDaoImpl(kodein) }
    bind<RolePermissionDao>() with singleton { RolePermissionDaoImpl(kodein) }

    bind<SubjectService>() with singleton { SubjectService(kodein) }
    bind<RoleService>() with singleton { RoleService(kodein) }

    bind<AuthorizingRealm>() with singleton { MyRealm(instance()) }
    bind<SessionManager>() with singleton { DefaultSessionManager() }
    bind<SecurityManager>() with singleton {
        DefaultSecurityManager(instance<AuthorizingRealm>()).apply {
            sessionManager = instance()
        }
    }
    bind<CarDao>() with singleton { CarDaoImpl(instance()) }
    bind<MechanicDao>() with singleton { MechanicDaoImpl(instance()) }
    bind<UserDao>() with singleton { UserDaoImpl(instance()) }
    bind<RentDao>() with singleton { RentDaoImpl(instance()) }
    bind<RepairDao>() with singleton { RepairDaoImpl(instance()) }
}

fun main(args: Array<String>) {
    // Initial Database bootstrap
    Utils.bootstrapDatabase(kodein)
    SecurityUtils.setSecurityManager(kodein.instance())
    embeddedServer(Netty, 8080, watchPaths = listOf("AppKt"), module = Application::module).start()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            kodein.instance<JdbcConnectionSource>().close()
        }
    })
}

fun Application.module() {
    install(Sessions) {
        cookie<MySession>("SESSIONID")
    }
    install(DefaultHeaders)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication) {
        basic("basic") {

            this.realm = kodein.instance<AuthorizingRealm>().name

            validate { credentials ->
                Logger.d(credentials)

                if (!SecurityUtils.getSubject().isAuthenticated) {
                    val token = UsernamePasswordToken(credentials.name, credentials.password)
                    token.isRememberMe = true
                    try {
                        SecurityUtils.getSubject().login(token)
                    } catch (e: Exception) {
                        val errMsg: String
                        val code: HttpStatusCode
                        when (e) {
                            is UnknownAccountException -> {
                                errMsg = "There is no subject with name of ${token.principal}"
                                code = HttpStatusCode.NotFound
                            }
                            is IncorrectCredentialsException -> {
                                errMsg = "Password for account ${token.principal} was incorrect!"
                                code = HttpStatusCode.Forbidden
                            }
                            is LockedAccountException -> {
                                errMsg = "The account for name ${token.principal} is locked.  " +
                                        "Please contact your administrator to unlock it."
                                code = HttpStatusCode.Forbidden
                            }
                            is AuthenticationException -> {
                                errMsg = e.message ?: ""
                                code = HttpStatusCode.InternalServerError
                            }
                            else -> throw e
                        }
                        handleError(errMsg, code)
                        return@validate null
                    }
                }
                return@validate IdPrincipal(SecurityUtils.getSubject().principal as Long)
            }
        }

    }
    install(Routing) {
        post("/signup") {
            val post = call.receive<Parameters>()
            val name = post["name"]
            val password = post["password"]
            if (name == null || name.isBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "name is null or blank")
                return@post
            } else if (password == null || password.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "password is null or blank")
                return@post
            }
            val subjectService: SubjectService = kodein.instance()
            if (subjectService.findSubjectByName(name) != null) {
                call.respond(HttpStatusCode.Conflict, "Subject with this name is already created")
            } else {
                val user = subjectService.createSubject(name, password)
                call.respond(HttpStatusCode.OK, user)
            }

        }
        authenticate("basic") {
            get("/login") {
                val currentSubjectId = call.principal<IdPrincipal>()?.id

                val sessionId = SecurityUtils.getSubject().session.id as String
                val mySession = MySession(sessionId)
                call.sessions.set(mySession)

                val subjectService: SubjectService = kodein.instance()

                if (currentSubjectId != null) {
                    val user = subjectService.findSubjectById(currentSubjectId)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
            }

        }

        get("/users") {
            val mySession = call.sessions.get<MySession>()
            try {
                if (mySession == null)
                    throw AuthorizationException("Could not find session cookie")

                val subject = Subject.Builder().sessionId(mySession.id).buildSubject()

                if (!subject.isAuthenticated)
                    throw AuthorizationException("Restored session is not authenticated")

                subject.checkPermission("users")
                val subjectService: SubjectService = kodein.instance()
                call.respond(HttpStatusCode.OK, subjectService.getAllSubjects())
            } catch (e: AuthorizationException) {
                Logger.err(e)
                call.respond(HttpStatusCode.Forbidden, e)
            }
        }
        delete("users/{id}") {
            val subjectId = call.parameters["id"]

            validateSession(call, "users:delete:$subjectId}") {
                val subjectService: SubjectService = kodein.instance()
                subjectService.getAllSubjects()
            }
        }

        get("/roles") {
            validateSession(call, "roles:read}") {
                val roleService: RoleService = kodein.instance()
                roleService.getAllRoles()
            }
        }
        /**
         * Delete role from roles collection and all relations with users
         */
        delete("/roles/{id}") {
            val roleId = call.parameters["id"]!!.toLong()

            validateSession(call, "roles:delete:$roleId}") {
                val roleService: RoleService = kodein.instance()
                roleService.deleteRole(roleId)

            }
        }


    }
}

suspend fun validateSession(call: ApplicationCall, permissionRequired: String, allowed: () -> Any) {
    try {
        val mySession = call.sessions.get<MySession>()
                ?: throw AuthorizationException("Could not find session cookie")

        val subject = Subject.Builder().sessionId(mySession.id).buildSubject()
        if (!subject.isAuthenticated) throw AuthorizationException("Restored session is not authenticated")

        subject.checkPermission(permissionRequired)
        subject.session.touch() // Refresh session
        val result = allowed()

        call.respond(HttpStatusCode.OK, result)
    } catch (e: AuthorizationException) {
        Logger.err(e)
        call.respond(HttpStatusCode.Unauthorized, e)
    }
}

suspend fun handleError(errMsg: String, statusCode: HttpStatusCode, call: ApplicationCall? = null) {
    Logger.err(errMsg)
    call?.respond(statusCode, errMsg)
}

