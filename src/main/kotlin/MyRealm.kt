
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
import service.SubjectService


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
        val subjectId = principals.fromRealm(name).iterator().next() as Long
        val subject = subjectService.findById(subjectId)
        return if (subject != null) {
            val info = SimpleAuthorizationInfo()
            subject.subjectRoles.forEach {
                info.addRole(it.role.name)
                info.addStringPermissions(it.role.permissions.map { it.permission })
            }
            return info
        } else null
    }

}