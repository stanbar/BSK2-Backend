package entrypoint

import data.user.UserDao
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection


class MyRealm(val userDao : UserDao) : AuthorizingRealm() {


    private fun getAccount(userName: String): SimpleAccount {
        val acc = SimpleAccount(userName, "password", name)
        acc.addRole("user")
        acc.addRole("admin")

        val user = userDao.findUser(userName)

        return SimpleAccount()

        return acc
    }

    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo {
        val userNameToken = token as UsernamePasswordToken
        return getAccount(userNameToken.username)
    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection?): AuthorizationInfo {
        val userName = getAvailablePrincipal(principals) as String
        return getAccount(userName)
    }

}