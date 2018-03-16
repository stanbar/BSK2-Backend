package entrypoint

import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import service.UserService


class MyRealm(val userService: UserService) : AuthorizingRealm() {
    init {
        name = "MyRealm"
        credentialsMatcher = HashedCredentialsMatcher()
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo? {
        val userNameToken = token as UsernamePasswordToken
        val user = userService.findUser(userNameToken.username)

        return if (user != null) {
            SimpleAuthenticationInfo(user.id, user.password, name)
        } else
            null
    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo? {
        val userId = principals.fromRealm(name).iterator().next() as Long
        val user = userService.getUser(userId)
        return if (user != null) {
            val info = SimpleAuthorizationInfo()
            user.roles.forEach {
                info.addRole(it.name)
                info.addStringPermissions(it.permissions)
            }
            return info
        } else null
    }

}