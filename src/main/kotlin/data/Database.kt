package data

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinAware
import com.github.salomonbrys.kodein.instance
import java.sql.DriverManager


class Database(override val kodein: Kodein) : KodeinAware {

    fun makeConnection() = DriverManager.getConnection(instance("dbPath"))

}