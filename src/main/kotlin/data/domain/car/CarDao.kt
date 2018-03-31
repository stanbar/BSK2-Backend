package data.domain.car

import com.j256.ormlite.dao.Dao

interface CarDao : Dao<Car, Long>{
    enum class Selector{
        BRAND, MODEL
    }
    fun findCarBy(selector: Selector)
}
