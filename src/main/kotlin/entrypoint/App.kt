package entrypoint

import com.stasbar.Logger
import data.role.RoleDao
import data.rolepermission.RolePermissionDaoImpl

import data.user.UserDao
import data.userrole.UserRolesDao
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.*
import org.apache.shiro.mgt.DefaultSecurityManager
import service.UserService


fun Application.module() {
    val database = Database()

    val userDao = UserDao(database)
    val roleDao = RoleDao(database)
    val rolePermissionDao = RolePermissionDaoImpl(database)
    val userRolesDao = UserRolesDao(database)
    database.bootstrap(userDao, roleDao, rolePermissionDao, userRolesDao)
    val userService = UserService(userDao, roleDao, userRolesDao)
    val myRealm = MyRealm(userService)

    val securityManager = DefaultSecurityManager(myRealm)
    SecurityUtils.setSecurityManager(securityManager)

    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {

        post("/login") {
            val post = call.receive<Parameters>()
            val username = post["username"]
            val password = post["password"]
            if (username.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "username is null or blank")
                return@post
            } else if (password.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "password is null or blank")
                return@post
            }

            val currentUserId = SecurityUtils.getSubject().principal

            val currentUser = if (currentUserId != null && currentUserId is Long)
                userDao.getUser(currentUserId)
            else
                null


            // let's login the current user so we can check against roles and permissions:
            if (!SecurityUtils.getSubject().isAuthenticated) {
                val token = UsernamePasswordToken(username, password)
                token.isRememberMe = true
                try {
                    SecurityUtils.getSubject().login(token)
                } catch (uae: UnknownAccountException) {
                    Logger.info("There is no user with username of " + token.principal)
                } catch (ice: IncorrectCredentialsException) {
                    Logger.info("Password for account " + token.principal + " was incorrect!")
                } catch (lae: LockedAccountException) {
                    Logger.info("The account for username " + token.principal + " is locked.  " +
                            "Please contact your administrator to unlock it.")
                } catch (ae: AuthenticationException) {
                    //unexpected condition?  error?
                }
                // ... catch more exceptions here (maybe custom ones specific to your application?
            }


        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, watchPaths = listOf("AppKt"), module = Application::module).start()
}