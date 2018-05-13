import IllegalRequestParameterException.IllegalParameterException
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.stasbar.Logger
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
import org.kodein.di.Kodein
import org.kodein.di.generic.*
import service.LoginNotFoundException
import service.RoleService
import service.SubjectService
import service.UserService

class IdPrincipal(val id: Long) : Principal
class MySession(val id: String)

/**
 * Dependency Injection Container
 */
val kodein = Kodein {
    constant("dbPath") with "jdbc:sqlite:mydatabase.db"
    bind<JdbcConnectionSource>() with provider { JdbcConnectionSource(instance("dbPath")) }
    bind<SubjectDao>() with singleton { SubjectDaoImpl(instance()) }
    bind<RoleDao>() with singleton { RoleDaoImpl(instance()) }
    bind<SubjectRolesDao>() with singleton { SubjectRolesDaoImpl(instance()) }
    bind<RolePermissionDao>() with singleton { RolePermissionDaoImpl(instance()) }

    bind<SubjectService>() with singleton { SubjectService(kodein) }
    bind<UserService>() with singleton { UserService(kodein) }
    bind<RoleService>() with singleton { RoleService(kodein) }

    bind<AuthorizingRealm>() with singleton { MyRealm(instance()) }
    bind<SessionManager>() with singleton { DefaultSessionManager() }
    bind<SecurityManager>() with singleton {
        DefaultSecurityManager(instance<AuthorizingRealm>()).apply {
            val sessionManager : SessionManager by kodein.instance()
            this.sessionManager = sessionManager
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
    val securityManager: SecurityManager by kodein.instance()
    SecurityUtils.setSecurityManager(securityManager)
    embeddedServer(Netty, 8080, watchPaths = listOf("AppKt"), module = Application::module).start()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            val connectionSource : JdbcConnectionSource by kodein.instance()
            connectionSource.close()
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

            val realm :AuthorizingRealm by kodein.instance()
            this.realm = realm.name


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
                                errMsg = "There is no subject with login of ${token.principal}"
                                code = HttpStatusCode.NotFound
                            }
                            is IncorrectCredentialsException -> {
                                errMsg = "Password for account ${token.principal} was incorrect!"
                                code = HttpStatusCode.Forbidden
                            }
                            is LockedAccountException -> {
                                errMsg = "The account for login ${token.principal} is locked.  " +
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
            val login = post["login"]
            val password = post["password"]
            if (login == null || login.isBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "login is null or blank")
                return@post
            } else if (password == null || password.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "password is null or blank")
                return@post
            }
            val subjectService: SubjectService by kodein.instance()
            try {
                subjectService.findSubjectByLogin(login)
                call.respond(HttpStatusCode.Conflict, "Subject with this login is already created")
            } catch (e: LoginNotFoundException) {
                val user = subjectService.createSubject(login, password)
                call.respond(HttpStatusCode.OK, user)
            }

        }
        authenticate("basic") {
            get("/login") {
                val currentSubjectId = call.principal<IdPrincipal>()?.id

                val sessionId = SecurityUtils.getSubject().session.id as String
                val mySession = MySession(sessionId)
                call.sessions.set(mySession)

                val userService: UserService by kodein.instance()

                if (currentSubjectId != null) {
                    val user = userService.findUserById(currentSubjectId)
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
                val subjectService: UserService by kodein.instance()
                call.respond(HttpStatusCode.OK, subjectService.getAllUsers())
            } catch (e: AuthorizationException) {
                Logger.err(e)
                call.respond(HttpStatusCode.Forbidden, e)
            }
        }

        get("/users/{id}") {
            validateSession(call, "roles:read}") {
                val userService: UserService by kodein.instance()
                try {
                    val id = call.parameters["id"]!!.toLong()
                    userService.findUserById(id)
                } catch (e: Exception) {
                    throw IllegalParameterException(call.parameters["id"])
                }

            }

        }

        get("/roles") {
            validateSession(call, "roles:read}") {
                val roleService: RoleService by kodein.instance()
                roleService.getAllRoles()
            }
        }


    }
}

suspend fun validateSession(call: ApplicationCall, permissionRequired: String, allowed: () -> Any?) {
    try {
        val mySession = call.sessions.get<MySession>()
                ?: throw AuthorizationException("Could not find session cookie")

        val subject = Subject.Builder().sessionId(mySession.id).buildSubject()
        if (!subject.isAuthenticated) throw AuthorizationException("Restored session is not authenticated")

        subject.checkPermission(permissionRequired)
        subject.session.touch() // Refresh session
        try {
            val result: Any? = allowed()
            if (result != null)
                call.respond(HttpStatusCode.OK, result)
            else
                call.respond(HttpStatusCode.NotFound)
        } catch (e: IllegalParameterException) {
            call.respond(HttpStatusCode.BadRequest, e.message)
        }


    } catch (e: AuthorizationException) {
        Logger.err(e)
        call.respond(HttpStatusCode.Unauthorized, e)
    }
}

suspend fun handleError(errMsg: String, statusCode: HttpStatusCode, call: ApplicationCall? = null) {
    Logger.err(errMsg)
    call?.respond(statusCode, errMsg)
}

