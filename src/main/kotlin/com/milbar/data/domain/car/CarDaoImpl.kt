package com.milbar.data.domain.car

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource


class CarDaoImpl(connectionSource: ConnectionSource) : BaseDaoImpl<Car, Long>(connectionSource, Car::class.java), CarDao {
    override fun findCarBy(selector: CarDao.Selector) {

    }
}