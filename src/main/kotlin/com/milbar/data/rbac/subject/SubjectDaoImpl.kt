package com.milbar.data.rbac.subject

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class SubjectDaoImpl(connectionSource: ConnectionSource) : BaseDaoImpl<Subject, Long>(connectionSource, Subject::class.java), SubjectDao