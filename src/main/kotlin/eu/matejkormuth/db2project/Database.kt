package eu.matejkormuth.db2project

import javax.sql.DataSource
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import java.sql.Connection

object Database {
    private lateinit var dataSource: DataSource

    fun initialize() {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgres://localhost:5432/police"
        config.username = "root"
        config.password = "root"
        config.dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
        dataSource = HikariDataSource(config)
    }

    fun getConnection(): Connection = dataSource.connection
}