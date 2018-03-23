package entrypoint

import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.response.respond


/**
 * Represents a Session authentication provider
 * @param name is the name of the provider, or `null` for a default provider
 */
class SessionAuthenticationProvider(name: String?) : AuthenticationProvider(name) {
    internal var authenticationFunction: suspend (Long) -> Principal? = { null }

    /**
     * Specifies realm to be passed in `WWW-Authenticate` header
     */
    var realm: String = "MyRealm"

    /**
     * Sets a validation function that will check given [UserPasswordCredential] instance and return [Principal],
     * or null if credential does not correspond to an authenticated principal
     */
    fun validate(body: suspend (Long) -> Principal?) {
        authenticationFunction = body
    }


}

/**
 * Installs Basic Authentication mechanism
 */
fun Authentication.Configuration.session(name: String? = null, configure: SessionAuthenticationProvider.() -> Unit) {
    val provider = SessionAuthenticationProvider(name).apply(configure)
    val realm = provider.realm
    val authenticate = provider.authenticationFunction

    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val id = call.request.sessionAuthenticationCredentials()
        val principal = id?.let { authenticate(it) }

        val cause = when {
            id == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(sessionAuthenticationChallengeKey, cause) {
                call.respond(UnauthorizedResponse(HttpAuthHeader.basicAuthChallenge(realm)))
                it.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    register(provider)
}

/**
 * Retrieves Basic authentication credentials for this [ApplicationRequest]
 */
fun ApplicationRequest.sessionAuthenticationCredentials(): Long? {
    val cookie = header("Cookie") ?: return null

    if (cookie.toLongOrNull() == null)
        throw IllegalArgumentException("Cookie is not Long: $cookie")

    return cookie.toLong()

}

private val sessionAuthenticationChallengeKey: Any = "SessionAuth"

