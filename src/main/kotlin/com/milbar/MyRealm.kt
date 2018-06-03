package com.milbar
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.crypto.hash.Sha256Hash
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import com.milbar.service.SubjectService


class MyRealm(private val subjectService: SubjectService) : AuthorizingRealm() {
    init {
        name = "MyRealm"
        credentialsMatcher = HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME)
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo? {
        val userNameToken = token as UsernamePasswordToken

        val user = subjectService.findBy(SubjectService.Selector.LOGIN.value, userNameToken.username)
        return if (user != null)
            SimpleAuthenticationInfo(user.id, user.password, name)
        else null

    }


    override fun doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo? {
        var subjectId: Long = -1L
        var roleId: Long = -1L

        for (principal in principals.fromRealm(name).iterator())
            when (principal) {
                is Long -> subjectId = principal
                is RolePrincipal -> roleId = principal.roleId
                is Any -> throw IllegalArgumentException("Could not handle this principal")
            }

        if(subjectId == -1L) throw IllegalArgumentException("Could not find required subjectId principal")
        if(roleId == -1L) throw IllegalArgumentException("Could not find required roleId principal")

        val subject = subjectService.findById(subjectId)

        return if (subject != null) {
            val info = SimpleAuthorizationInfo()
            val subjectRole = subject.subjectRoles.find { it.role.id == roleId } ?: return null
            info.addRole(subjectRole.role.name)
            info.addStringPermissions(subjectRole.role.permissions.map { it.permission })

            return info
        } else null
    }

}