package data.rbac.subject_role

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class SubjectRolesDaoImpl(connectionSource: ConnectionSource) : BaseDaoImpl<SubjectRole, Void>(connectionSource, SubjectRole::class.java), SubjectRolesDao