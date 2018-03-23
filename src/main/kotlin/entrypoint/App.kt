package entrypoint

import com.stasbar.Logger
import data.Database
import data.role.RoleDao
import data.rolepermission.RolePermissionDaoImpl
import data.user.UserDao
import data.userrole.UserRolesDao
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
import org.apache.shiro.session.Session
import org.apache.shiro.session.SessionListener
import org.apache.shiro.session.mgt.DefaultSessionManager
import org.apache.shiro.subject.Subject
import service.RoleService
import service.UserService

class IdPrincipal(val id: Long) : Principal

val sessionListener = object : SessionListener {
    override fun onExpiration(session: Session?) {
        Logger.d("Session expired: ${session?.id}")
    }

    override fun onStart(session: Session?) {
        Logger.d("Session started: ${session?.id}")
    }

    override fun onStop(session: Session?) {
        Logger.d("Session stopped: ${session?.id}")
    }
}

class MySession(val id: String)

fun Application.module() {
    val database = Database()

    val userDao = UserDao(database)
    val roleDao = RoleDao(database)
    val rolePermissionDao = RolePermissionDaoImpl(database)
    val userRolesDao = UserRolesDao(database)
    database.bootstrap(userDao, roleDao, rolePermissionDao, userRolesDao)
    val userService = UserService(userDao, roleDao, userRolesDao, rolePermissionDao)
    val roleService = RoleService(roleDao, rolePermissionDao)
    val myRealm = MyRealm(userService)


    val securityManager = DefaultSecurityManager(myRealm)


    val sessionManager = DefaultSessionManager()
    sessionManager.sessionListeners = listOf(sessionListener)


    securityManager.sessionManager = sessionManager
    SecurityUtils.setSecurityManager(securityManager)

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
            realm = myRealm.name

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
                                errMsg = "There is no user with username of ${token.principal}"
                                code = HttpStatusCode.NotFound
                            }
                            is IncorrectCredentialsException -> {
                                errMsg = "Password for account ${token.principal} was incorrect!"
                                code = HttpStatusCode.Forbidden
                            }
                            is LockedAccountException -> {
                                errMsg = "The account for username ${token.principal} is locked.  " +
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
//        session("session") {
//            validate {
//                val subject = Subject.Builder().sessionId(it).buildSubject()
//                if(subject.isAuthenticated)
//                    return@validate IdPrincipal(subject.principal as Long)
//                else
//                    return@validate null
//
//            }
//        }
    }
    install(Routing) {
        post("/signup") {
            val post = call.receive<Parameters>()
            val username = post["username"]
            val password = post["password"]
            if (username == null || username.isBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "username is null or blank")
                return@post
            } else if (password == null || password.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "password is null or blank")
                return@post
            }

            if (userService.findUserByName(username) != null) {
                call.respond(HttpStatusCode.Conflict, "User with this name is already created")
            } else {
                val user = userService.createUser(username, password)
                call.respond(HttpStatusCode.OK, user)
            }

        }
        authenticate("basic") {
            get("/login") {
                val currentUserId = call.principal<IdPrincipal>()?.id

                val sessionId = SecurityUtils.getSubject().session.id as String
                val mySession = MySession(sessionId)
                call.sessions.set(mySession)

                if (currentUserId != null) {
                    val user = userService.findUserById(currentUserId)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find user after successful login")
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

                call.respond(HttpStatusCode.OK, userService.getAllUsers())
            } catch (e: AuthorizationException) {
                Logger.err(e)
                call.respond(HttpStatusCode.Forbidden, e)
            }
        }
        get("/roles") {
            try {
                SecurityUtils.getSubject().checkPermission("roles")
                call.respond(HttpStatusCode.OK, roleService.getAllRoles())
            } catch (e: AuthorizationException) {
                Logger.err(e)
                call.respond(HttpStatusCode.Forbidden, e)
            }
        }

    }
}

suspend fun handleError(errMsg: String, statusCode: HttpStatusCode, call: ApplicationCall? = null) {
    Logger.err(errMsg)
    call?.respond(statusCode, errMsg)
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, watchPaths = listOf("AppKt"), module = Application::module).start()
}
