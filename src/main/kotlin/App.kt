import com.j256.ormlite.support.ConnectionSource
import com.stasbar.Logger
import exception.IllegalParameterException
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Locations
import io.ktor.request.receiveParameters
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.*
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
import routing.*
import service.UserService

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
    install(ContentNegotiation) { gson { setPrettyPrinting() } }
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

        val userService: UserService by kodein.instance()
        authenticate("basic") {
            accept(ContentType.Text.Html) {
                get("/myRoles") {
                    val currentSubjectId = call.principal<IdPrincipal>()?.id

                    if (currentSubjectId != null) {
                        val user = userService.findById(currentSubjectId)
                        if (user != null) {
                            val roles = user.subject.subjectRoles.map {
                                val role = it.role
                                hashMapOf("id" to role.id, "name" to role.name, "description" to role.description)
                            }

                            call.respond(FreeMarkerContent("myRoles.ftl", mapOf("login" to user.subject.login, "roles" to roles)))
                        } else {
                            call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "Could not find subject after successful login")))

                        }
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
                }
                post("/login") {
                    val parameters = call.receiveParameters()
                    val selectedRoleId = parameters["roleId"]?.toLongOrNull()
                    if (selectedRoleId == null) {
                        call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                        call.response.header("Reason", "roleId is null or blank")
                        return@post
                    }

                    val currentSubjectId = call.principal<IdPrincipal>()?.id
                    if (currentSubjectId != null) {
                        val user = userService.findById(currentSubjectId)
                        if (user != null) {
                            val currentRole = user.subject.subjectRoles.find { it.role.id == selectedRoleId }
                            if (currentRole != null) {
                                val sessionId = SecurityUtils.getSubject().session.id as String
                                val mySession = MySession(sessionId, selectedRoleId)

                                call.sessions.set(mySession)

                                //Prevent from exposing all user roles
                                user.subject.subjectRoles = user.subject.subjectRoles.filter { it.role.id == selectedRoleId }
                                call.respond(FreeMarkerContent("user.ftl", mapOf("user" to user)))
                            } else {
                                call.respond(FreeMarkerContent("error.ftl", mapOf("error" to "You don't have role with id \"$selectedRoleId\"")))
                            }

                        } else
                            call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
                }
            }
            accept(ContentType.Application.Json) {
                get("/myRoles") {
                    val currentSubjectId = call.principal<IdPrincipal>()?.id

                    if (currentSubjectId != null) {
                        val user = userService.findById(currentSubjectId)
                        if (user != null) {
                            val response = user.subject.subjectRoles.map {
                                val role = it.role
                                hashMapOf("id" to role.id, "name" to role.name, "description" to role.description)
                            }
                            call.respond(HttpStatusCode.OK, response)
                        } else
                            call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
                }
                post("/login") {
                    val parameters = call.receiveParameters()
                    val selectedRoleId = parameters["roleId"]?.toLongOrNull()
                    if (selectedRoleId == null) {
                        call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                        call.response.header("Reason", "roleId is null or blank")
                        return@post
                    }

                    val currentSubjectId = call.principal<IdPrincipal>()?.id
                    if (currentSubjectId != null) {
                        val user = userService.findById(currentSubjectId)
                        if (user != null) {
                            val currentRole = user.subject.subjectRoles.find { it.role.id == selectedRoleId }
                            if (currentRole != null) {
                                val sessionId = SecurityUtils.getSubject().session.id as String
                                val mySession = MySession(sessionId, selectedRoleId)

                                call.sessions.set(mySession)

                                //Prevent from exposing all user roles
                                user.subject.subjectRoles = user.subject.subjectRoles.filter { it.role.id == selectedRoleId }
                                call.respond(HttpStatusCode.OK, user)
                            } else {
                                call.respond(HttpStatusCode.Unauthorized, "You don't have role with id \"$selectedRoleId\"")
                            }

                        } else
                            call.respond(HttpStatusCode.InternalServerError, "Could not find subject after successful login")
                    } else
                        call.respond(HttpStatusCode.InternalServerError, "Subject's principal was null or not Long")
                }

            }
        }
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

