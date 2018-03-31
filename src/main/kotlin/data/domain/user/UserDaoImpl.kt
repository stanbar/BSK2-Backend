package data.domain.user

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class UserDaoImpl(connectionSource: ConnectionSource) : BaseDaoImpl<User, Long>(connectionSource, User::class.java), UserDao