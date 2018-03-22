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
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.*
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.mgt.DefaultSecurityManager
import service.UserService

class IdPrincipal(val id: Long) : Principal

fun Application.module() {
    val database = Database()

    val userDao = UserDao(database)
    val roleDao = RoleDao(database)
    val rolePermissionDao = RolePermissionDaoImpl(database)
    val userRolesDao = UserRolesDao(database)
    database.bootstrap(userDao, roleDao, rolePermissionDao, userRolesDao)
    val userService = UserService(userDao, roleDao, userRolesDao, rolePermissionDao)
    val myRealm = MyRealm(userService)

    val securityManager = DefaultSecurityManager(myRealm)
    SecurityUtils.setSecurityManager(securityManager)

    install(DefaultHeaders)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Authentication) {
        basic(myRealm.name) {

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
        authenticate(myRealm.name) {


            get("/whoami") {
                val currentUserId = call.principal<IdPrincipal>()?.id

                if (currentUserId != null) {
                    val user = userService.findUserById(currentUserId)
                    if (user != null)
                        call.respond(HttpStatusCode.OK, user)
                    else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find user after successful login")
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
            }
            get("/users") {
                val currentUserId = call.principal<IdPrincipal>()?.id
                try {
                    SecurityUtils.getSubject().checkPermission("user")
                } catch (e: AuthorizationException) {
                    Logger.err(e)
                }
                if (currentUserId != null) {
                    val user = userService.findUserById(currentUserId)
                    if (user != null)
                        call.respond(HttpStatusCode.OK, userService.getAllUsers())
                    else
                        call.respond(HttpStatusCode.InternalServerError, "Could not find user after successful login")
                } else
                    call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
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
