package entrypoint

import com.stasbar.Logger
import data.user.UserDAOImpl
import data.user.UserDao
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.*
import org.apache.shiro.config.IniSecurityManagerFactory


fun Application.module() {
    val factory = IniSecurityManagerFactory("classpath:shiro.ini")
    val securityManager = factory.instance
    SecurityUtils.setSecurityManager(securityManager)

    val database = Database()

    val userDao : UserDao = UserDAOImpl(database)
    val myRealm = MyRealm(userDao)

    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        route("/who"){

        }
        get("/user"){


            val currentUser = SecurityUtils.getSubject()
            // let's login the current user so we can check against roles and permissions:
            if (!currentUser.isAuthenticated) {
                val token = UsernamePasswordToken("lonestarr", "vespa")
                token.isRememberMe = true
                try {
                    currentUser.login(token)
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