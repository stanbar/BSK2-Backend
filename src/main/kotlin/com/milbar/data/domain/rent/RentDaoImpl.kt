package com.milbar.data.domain.rent

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class RentDaoImpl (connectionSource: ConnectionSource)
    : BaseDaoImpl<Rent, Long>(connectionSource, Rent::class.java), RentDao