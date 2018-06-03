package com.milbar
import com.j256.ormlite.support.ConnectionSource
import com.stasbar.Logger
import com.milbar.exception.IllegalParameterException
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.Principal
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.*
import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.mgt.DefaultSubjectFactory
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.support.DefaultSubjectContext
import org.kodein.di.generic.instance
import org.slf4j.event.Level

class IdPrincipal(val id: Long) : Principal
class RolePrincipal(val roleId: Long) : Principal
class MySession(val id: String, val roleId: Long)


fun Application.main() {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            val connectionSource: ConnectionSource by kodein.instance()
            connectionSource.close()
        }
    })
    val realm: AuthorizingRealm by kodein.instance()
    val securityManager: SecurityManager by kodein.instance()
    SecurityUtils.setSecurityManager(securityManager)
    Utils.bootstrapDatabase(kodein)

    install(DefaultHeaders)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(CallLogging){
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/login") }
        filter { call -> call.request.path().startsWith("/myRoles") }
    }
    install(Sessions) { cookie<MySession>("SESSIONID") }
    install(FreeMarker) { templateLoader = ClassTemplateLoader(MyRealm::class.java.classLoader, "templates") }
    install(Locations)
    install(Authentication) {
        basic("basic") {
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
        index()
        login()
        signup()
        users()
        roles()
        permissions()
    }
}

suspend fun validateSession(call: ApplicationCall, permissionRequired: String, allowed: () -> Any?) {
    val realm: AuthorizingRealm by kodein.instance()

    try {
        val mySession = call.sessions.get<MySession>()
                ?: throw AuthorizationException("Could not find session cookie")

        val subject = Subject.Builder()
                .sessionId(mySession.id)
                .buildSubject()

        // Recreate Subject with additional rolePrincipal
        val rolePrincipal = RolePrincipal(mySession.roleId)

        val subjectContext = DefaultSubjectContext().apply {
            session = subject.session
            isAuthenticated = subject.isAuthenticated
            principals = SimplePrincipalCollection(listOf(subject.principal, rolePrincipal), realm.name)
        }
        val subjectRecreated = DefaultSubjectFactory().createSubject(subjectContext)

        if (!subjectRecreated.isAuthenticated)
            throw AuthorizationException("Restored session is not authenticated")

        subjectRecreated.checkPermission(permissionRequired)
        subjectRecreated.session.touch() // Refresh session
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

