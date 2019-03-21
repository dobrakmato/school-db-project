package eu.matejkormuth.db2project

import java.lang.StringBuilder
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types

class QueryBuilder<T : Entity>(private val table: Table<T>, private val connection: Connection) {

    sealed class Param {
        data class IntParam(val value: Int?) : Param()
        data class StringParam(val value: String?) : Param()
    }

    private val sql = StringBuilder()
    private var where = false
    private val parameters = mutableListOf<Param>()

    fun select(columns: Iterable<String>? = null): QueryBuilder<T> = fluent {
        sql.append("SELECT ")

        if (columns == null) {
            sql.append("*")
        } else {
            sql.append(columns.joinToString(", ") { escape(it) })
        }

        sql.append(" FROM ${escape(table.name)}")
    }

    fun insertOne(row: T): Lazy<T> {
        val columns = mutableListOf<String>()
        val values = mutableListOf<String>()

        table.columns.values.forEach {
            // Do not insertOne any number for auto incremented columns (currently on PK Id).
            if (!it.isId) {
                columns.add(escape(it.name))
                values.add("?")

                val param: Param = when {
                    it.isInty -> Param.IntParam(it.valueFor(row)?.toInt())
                    it.isStringy -> Param.StringParam(it.valueFor(row))
                    else -> throw UnsupportedOperationException("Column $it is not inty nor stringy!")
                }
                parameters.add(param)
            }
        }

        sql.append("INSERT INTO ${escape(table.name)} (${columns.joinToString(", ")}) VALUES (${values.joinToString(",")}) RETURNING id")

        log.debug(sql.toString())

        val autoIncrementedId = createBoundStatement().use { stmt ->
            stmt.execute()
            stmt.resultSet.use {
                it.next()
                it.getInt(1)
            }
        }


        return Lazy(autoIncrementedId)
    }

    fun updateOne(entity: T) = fluent {
        val kvPairs = mutableListOf<String>()

        table.columns.values.filter { !it.isId }.forEach {
            kvPairs += "${it.name} = ?"

            val param: Param = when {
                it.isInty -> Param.IntParam(it.valueFor(entity)?.toInt())
                it.isStringy -> Param.StringParam(it.valueFor(entity))
                else -> throw UnsupportedOperationException("Column $it is not inty nor stringy!")
            }
            parameters.add(param)
        }


        sql.append("UPDATE ${table.name} SET ${kvPairs.joinToString(", ")}")

        /* if this entity has unique AI PK id column */
        if (table.hasId) {
            updateById(table.extractId(entity))
        } else {
            throw UnsupportedOperationException("Cannot update entity which has no ID column.")
        }

        createBoundStatement().use { stmt ->
            stmt.executeUpdate()
        }
    }

    private fun updateById(id: Id) = eq("id", id)

    fun delete(): QueryBuilder<T> = fluent { sql.append("DELETE FROM ${escape(table.name)}") }

    fun <K> eq(column: String, value: K) = where(column, "=", value)

    fun <K> where(column: String, op: String, value: K): QueryBuilder<T> = fluent {
        if (!where) sql.append(" WHERE ")
        sql.append(escape(column))
        sql.append(' ')
        sql.append(op)
        sql.append(" ?")

        if (column !in table.columns) throw IllegalArgumentException("Column $column does not exsits on ${table.name}")
        val it = table.columns.getValue(column)

        val param: Param = when {
            it.isInty -> Param.IntParam(value.toString().toInt())
            it.isStringy -> Param.StringParam(it.toString())
            else -> throw UnsupportedOperationException("Column $it is not inty nor stringy!")
        }
        parameters.add(param)
        where = true
    }

    fun limit(limit: Int): QueryBuilder<T> = fluent { sql.append(" LIMIT $limit") }

    fun fetchOne(): T? = fetchMultiple().firstOrNull()

    fun fetchMultiple(): Iterable<T> {
        val list = mutableListOf<T>()

        createBoundStatement().use { stmt ->
            stmt.executeQuery().use {
                while (it.next()) {
                    list.add(table.instantiate(it))
                }
            }

        }
        return list
    }

    fun execute(): Boolean = createBoundStatement().use { it.execute() }


    private fun createBoundStatement(): PreparedStatement {
        val dbgSql = sql.toString()

        val stmt = connection.prepareStatement(sql.toString())

        /* bind parameters */
        parameters.forEachIndexed { index, param ->
            when (param) {
                is Param.IntParam -> stmt.setObject(index + 1, param.value, Types.INTEGER)
                is Param.StringParam -> stmt.setObject(index + 1, param.value, Types.VARCHAR)
            }
        }

        log.debug("SQL: $dbgSql")
        log.debug(" PARAMETERS: $parameters")

        return stmt
    }

    companion object {
        val log by logger()
        fun escape(name: String): String = name
    }

}