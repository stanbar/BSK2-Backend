package data

import entrypoint.Database
import java.sql.ResultSet
import java.sql.Statement

open class DaoImpl(val database: Database){
    protected fun execute(sql: String): Long {
        database.makeConnection().use {
            it.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use {
                it.execute()
                val keys = it.generatedKeys
                keys.next()
                return keys.getLong(1)
            }
        }
    }

    protected fun select(sql: String): ResultSet {
        database.makeConnection().use {
            it.createStatement().use {
                return it.executeQuery(sql)
            }
        }
    }
}