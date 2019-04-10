package eu.matejkormuth.db2project

import javax.sql.DataSource
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.sql.Connection

object Database {
    private lateinit var dataSource: DataSource
    private val tableCache: MutableMap<Class<*>, Table<*>> = hashMapOf()

    fun initialize() {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
        config.username = "postgres"
        config.password = "root"
        config.isAutoCommit = false
        dataSource = HikariDataSource(config)
    }

    fun getConnection(): Connection = dataSource.connection

    @Suppress("UNCHECKED_CAST")
    fun <T : Entity> tableFor(klass: Class<T>): Table<T> {
        return tableCache.getOrPut(klass) { Table(klass) } as Table<T>
    }
}