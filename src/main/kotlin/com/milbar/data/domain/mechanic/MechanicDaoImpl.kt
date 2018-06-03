package com.milbar.data.domain.mechanic

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class MechanicDaoImpl(connectionSource: ConnectionSource)
    : BaseDaoImpl<Mechanic, Long>(connectionSource, Mechanic::class.java), MechanicDao