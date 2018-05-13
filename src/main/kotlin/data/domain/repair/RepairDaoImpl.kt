package data.domain.repair

import com.j256.ormlite.dao.BaseDaoImpl
import com.j256.ormlite.support.ConnectionSource

class RepairDaoImpl(connectionSource: ConnectionSource)
    : BaseDaoImpl<Repair, Long>(connectionSource, Repair::class.java), RepairDao