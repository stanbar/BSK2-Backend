package entrypoint

import org.apache.shiro.crypto.hash.Sha256Hash
import java.sql.DriverManager


class Database {
    val DB_PATH = ":memory:" // "mydatabase.db"


    init {
        makeConnection().use {
            it.createStatement().use{
                it.queryTimeout = 30;  // set timeout to 30 sec.
                it.executeUpdate("INSERT INTO roles VALUES (1, 'user', rolepermission)")
                it.executeUpdate("INSERT INTO roles VALUES (2, 'admin', 'The administrator role only given to site admins')")
                it.executeUpdate("INSERT INTO roles_permissions VALUES (2, 'user:*')")
                it.executeUpdate("insert into users(id,username,email,password) values (1, 'admin', 'sample@shiro.apache.org', '" + Sha256Hash("admin").toHex() + "')")
                it.executeUpdate("INSERT INTO users_roles VALUES (1, 2)")
            }

        }
    }

    fun makeConnection() = DriverManager.getConnection("jdbc:sqlite:$DB_PATH")

}