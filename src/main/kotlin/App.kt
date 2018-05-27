
import com.j256.ormlite.support.ConnectionSource
import com.stasbar.Logger
import exception.IllegalParameterException
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.HttpsRedirect
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
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
import service.RoleService
import service.SubjectService
import service.UserService
import java.io.File
import java.security.KeyStore

class IdPrincipal(val id: Long) : Principal
class RolePrincipal(val roleId: Long) : Principal
class MySession(val id: String, val roleId: Long)

fun main(args: Array<String>) {
    // Initial Database bootstrap
    Utils.bootstrapDatabase(kodein)
    val securityManager: SecurityManager by kodein.instance()
    SecurityUtils.setSecurityManager(securityManager)

    val env = applicationEngineEnvironment {
        module {
            module()
        }
        connector {
            host = "0.0.0.0"
            port = 8080
        }
        sslConnector(keyStore = KeyStore.getInstance("JKS").apply { load(File("keystoreBSK2.jks").inputStream(), "Password123".toCharArray()) },
                keyAlias = "bsk2",
                keyStorePassword = { "Password123".toCharArray() },
                privateKeyPassword = { "Password123".toCharArray() }) {
            port = 8443
            keyStorePath = File("keystoreBSK2.jks").absoluteFile
        }

    }
    embeddedServer(Netty, env).start()

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            val connectionSource: ConnectionSource by kodein.instance()
            connectionSource.close()
        }
    })
}


fun Application.module() {
    val realm: AuthorizingRealm by kodein.instance()

    install(Sessions) {
        cookie<MySession>("SESSIONID")
    }
    install(DefaultHeaders)
    install(HttpsRedirect) {
        sslPort = 8443
    }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()

        }
    }
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

        val userService: UserService by kodein.instance()
        val subjectService: SubjectService by kodein.instance()

        post("/signup") {
            val post = call.receive<Parameters>()
            val login = post["login"]
            val firstName = post["firstName"]
            val lastName = post["lastName"]
            val driverLicence = post["driverLicence"]
            val PESEL = post["PESEL"]
            val password = post["password"]
            if (login == null || login.isBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "login is null or blank")
                return@post
            } else if (password == null || password.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "password is null or blank")
                return@post
            } else if (firstName == null || firstName.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "firstName is null or blank")
                return@post
            } else if (lastName == null || lastName.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "lastName is null or blank")
                return@post
            } else if (driverLicence == null || driverLicence.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "driverLicence is null or blank")
                return@post
            } else if (PESEL == null || PESEL.isNullOrBlank()) {
                call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                call.response.header("Reason", "PESEL is null or blank")
                return@post
            }



            if (subjectService.findBy(SubjectService.Selector.LOGIN.value, login) != null)
                call.respond(HttpStatusCode.Conflict, "Subject with this login is already created")
            else {
                val createdUser = userService.createUser(login, password, firstName, lastName, PESEL, driverLicence)
                call.respond(HttpStatusCode.OK, createdUser)
            }
        }
        authenticate("basic") {
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


        get("/users") {
            validateSession(call, "user:read:*") {
                subjectService.getAll()
            }
        }

        get("/users/{id}") {
            try {
                val id = call.parameters["id"]!!.toLong()
                validateSession(call, "user:read:$id") {
                    userService.findById(id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw IllegalParameterException(call.parameters["id"])
            }

        }

        get("/roles") {
            validateSession(call, "role:read:*") {
                val roleService: RoleService by kodein.instance()
                roleService.getAll()
            }
        }
        get("/") {
            call.respond(HttpStatusCode.OK, "BSK2")
        }

        //TODO remove it, security leak
        get("/grand/{subjectId}/{roleId}") {
            try {
                val subjectId = call.parameters["subjectId"]!!.toLong()
                val roleId = call.parameters["roleId"]!!.toLong()

                val roleService: RoleService by kodein.instance()
                val role = roleService.findById(roleId)
                val subject = subjectService.findById(subjectId)
                roleService.addRoleToSubject(role!!, subject!!)
                call.respond(subjectService.findById(subjectId)!!)

            } catch (e: Exception) {
                throw IllegalParameterException("${call.parameters["subjectId"]} and ${call.parameters["roleId"]}")
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

