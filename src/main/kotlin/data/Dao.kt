package data

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.instance
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

abstract class Dao(kodein: Kodein) {

    val database: Database = kodein.instance()

    abstract val TABLE_NAME: String
    abstract val CREATE: String

    fun recreate() {
        execute("DROP TABLE IF EXISTS $TABLE_NAME")
        execute(CREATE)
    }

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

    protected fun connect() = database.makeConnection()

    protected fun Connection.query(
            sql: String,
            action: (PreparedStatement.() -> Unit)? = null
    ): PreparedStatement {

        prepareStatement(sql).let {
            action?.invoke(it)
            return it
        }
    }

}


