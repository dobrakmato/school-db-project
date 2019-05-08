package eu.matejkormuth.db2project

import java.lang.StringBuilder
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.ZoneId

/**
 * SQL query builder. Provides type-safe alternative to building queries.
 */
class QueryBuilder<T : Entity>(private val table: Table<T>, private val connection: Connection) {

    sealed class Param {
        data class IntParam(val value: Int?) : Param()
        data class BooleanParam(val value: Boolean?) : Param()
        data class StringParam(val value: String?) : Param()
        data class TimestampParam(val value: Instant?) : Param()
    }

    private val sql = StringBuilder()
    private var where = false
    private var insert = false
    private var eager = false
    private val parameters = mutableListOf<Param>()
    private val aliasIterator = ('a'..'z').iterator()
    private val aliases = mutableMapOf<TableField, String>()

    /**
     * Adds select clause.
     */
    fun select(columns: Iterable<String>? = null): QueryBuilder<T> = fluent {
        sql.append("SELECT ")

        if (columns == null) {
            sql.append("*")
        } else {
            sql.append(columns.joinToString(", ") { escape(it) })
        }

        sql.append(" FROM ${escape(table.name)}")

    }

    /**
     * Adds select with joins clause.
     */
    fun selectEager(): QueryBuilder<T> = fluent {
        eager = true

        sql.append("SELECT ")

        val pairs = mutableListOf<Pair<String, String>>()

        // aliasIterator from this table
        pairs.addAll(table.columns.values.map { "${table.name}.${it.name}" to "${table.name}_${it.name}" })

        /* generate aliasIterator for referenced tables */
        table.columns.values.filter { it.isReference }.forEach { aliases[it] = aliasIterator.next().toString() }

        // aliasIterator from all foreign tables
        table.columns.values.filter { it.isReference }.forEach { foreignKey ->
            val foreignTable = foreignKey.table
            val foreignTableAlias = aliases[foreignKey]
            pairs.addAll(foreignTable.columns.values.map { "$foreignTableAlias.${it.name}" to "${foreignTableAlias}_${it.name}" })
        }

        sql.append(pairs.joinToString(", ") { "${it.first} as ${it.second}" })

        sql.append(" FROM ${escape(table.name)}")

        // find all join tables
        table.columns.values.filter { it.isReference }.forEach {
            val refTableName = it.table.name
            sql.append(" LEFT OUTER JOIN $refTableName ${aliases[it]} ON ${table.name}.${it.name} = ${aliases[it]}.id")
        }
    }

    /**
     * Terminal operation: runs sql statement and inserts specified row.
     */
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
                    it.isBoolean -> Param.BooleanParam(it.booleanFor(row))
                    it.isStringy -> Param.StringParam(it.valueFor(row))
                    it.type == Instant::class.java -> Param.TimestampParam(it.instantFor(row))
                    else -> throw UnsupportedOperationException("Column $it is not inty nor stringy nor boolean!")
                }
                parameters.add(param)
            }
        }

        sql.append("INSERT INTO ${escape(table.name)} (${columns.joinToString(", ")}) VALUES (${values.joinToString(", ")}) RETURNING id")

        val autoIncrementedId = createBoundStatement().use { stmt ->
            stmt.execute()
            stmt.resultSet.use {
                it.next()
                it.getInt(1)
            }
        }


        return Lazy(autoIncrementedId)
    }

    /**
     * Perform multi-insert. Make multiple calls to this method to insert multiple entities.
     */
    fun insertMultiple(one: T) {
        var wasFirst = false
        if (!insert) {
            val columns = table.columns.values.filter { !it.isId }.map { escape(it.name) }
            sql.append("INSERT INTO ${escape(table.name)} (${columns.joinToString(", ")}) VALUES")
            wasFirst = true
            insert = true
        }


        val values = mutableListOf<String>()

        table.columns.values.forEach {
            // Do not insertOne any number for auto incremented columns (currently on PK Id).
            if (!it.isId) {
                values.add("?")

                val param: Param = when {
                    it.isInty -> Param.IntParam(it.valueFor(one)?.toInt())
                    it.isBoolean -> Param.BooleanParam(it.booleanFor(one))
                    it.isStringy -> Param.StringParam(it.valueFor(one))
                    it.type == Instant::class.java -> Param.TimestampParam(it.instantFor(one))
                    else -> throw UnsupportedOperationException("Column $it is not inty nor stringy nor boolean!")
                }
                parameters.add(param)
            }
        }

        /* Add comma between all rows except the first row. */
        if (!wasFirst) sql.append(", ")

        sql.append("(${values.joinToString(", ")})")

    }

    /**
     * Terminal operation: runs sql statement and updates specified row.
     */
    fun updateOne(entity: T) {
        val kvPairs = mutableListOf<String>()

        table.columns.values.filter { !it.isId }.forEach {
            kvPairs += "${it.name} = ?"

            val param: Param = when {
                it.isInty -> Param.IntParam(it.valueFor(entity)?.toInt())
                it.isBoolean -> Param.BooleanParam(it.booleanFor(entity))
                it.isStringy -> Param.StringParam(it.valueFor(entity))
                it.type == Instant::class.java -> Param.TimestampParam(it.instantFor(entity))
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

    /**
     * Adds delete clause.
     */
    fun delete(): QueryBuilder<T> = fluent { sql.append("DELETE FROM ${escape(table.name)}") }

    /**
     * Adds equals clause.
     */
    fun <K> eq(column: String, value: K) = where(column, "=", value)

    /**
     * Adds and word.
     */
    fun and() = fluent { sql.append(" AND") }

    /**
     * Adds where clause.
     */
    fun <K> where(column: String, op: String, value: K) = fluent {
        var col = column /* Need this to strip the table name in some cases */

        if (!where) sql.append(" WHERE")
        sql.append(' ')
        sql.append(column)
        sql.append(' ')
        sql.append(op)
        sql.append(" ?")

        /* Check for column name and for "table_name.column_name" */
        if (col !in table.columns) {
            if (col.split('.').last() !in table.columns) {
                throw IllegalArgumentException("Column $column does not exists on ${table.name}")
            }
            col = col.split(".").last()
        }
        val it = table.columns.getValue(col)

        val param: Param = when {
            it.isInty -> Param.IntParam(value.toString().toInt())
            it.isStringy -> Param.StringParam(it.toString())
            else -> throw UnsupportedOperationException("Column $it is not inty nor stringy!")
        }
        parameters.add(param)
        where = true
    }

    /**
     * Adds limit clause.
     */
    fun limit(limit: Int): QueryBuilder<T> = fluent { sql.append(" LIMIT $limit") }

    /**
     * Adds for update modifier.
     */
    fun forUpdate() = fluent { sql.append(" FOR UPDATE") }

    /**
     * Terminal operation: fetches first row.
     */
    fun fetchOne(): T? = fetchMultiple().firstOrNull()

    /**
     * Adds order by clause.
     */
    fun orderBy(column: String) = fluent {
        sql.append(" ORDER BY ${if (eager) "${table.name}." else ""}$column")
    }

    /**
     * Terminal operation: fetches multiple rows from database.
     */
    fun fetchMultiple(): Iterable<T> {
        val list = mutableListOf<T>()

        createBoundStatement().use { stmt ->
            stmt.executeQuery().use {
                while (it.next()) {
                    list.add(table.instantiate(it, eager, if (aliases.isEmpty()) null else aliases))
                }
            }

        }
        return list
    }

    /**
     * Executes built statement.
     */
    fun execute(): Boolean = createBoundStatement().use { it.execute() }

    /**
     * Creates PreparedStatement and bind all parameters to it.
     */
    private fun createBoundStatement(): PreparedStatement {
        var dbgSql = sql.toString()

        val stmt = connection.prepareStatement(sql.toString())

        /* bind parameters */
        parameters.forEachIndexed { index, param ->
            when (param) {
                is Param.IntParam -> stmt.setObject(index + 1, param.value, Types.INTEGER)
                is Param.BooleanParam -> stmt.setObject(index + 1, param.value, Types.BOOLEAN)
                is Param.StringParam -> stmt.setObject(index + 1, param.value, Types.VARCHAR)
                is Param.TimestampParam -> stmt.setObject(index + 1, if (param.value != null) Timestamp.from(param.value) else null, Types.TIMESTAMP)
            }
        }

        /* incorporate parameters to actual query */
        parameters.forEach {
            dbgSql = when (it) {
                is Param.IntParam -> dbgSql.replaceFirst("?", it.value.toString())
                is Param.BooleanParam -> dbgSql.replaceFirst("?", it.value.toString())
                is Param.StringParam -> dbgSql.replaceFirst("?", it.value ?: "null")
                is Param.TimestampParam -> dbgSql.replaceFirst("?", it.value?.atZone(ZoneId.of("Europe/Bratislava"))?.toLocalDateTime()?.toString()
                        ?: "null")
            }

        }
        log.debug("SQL: $dbgSql")

        return stmt
    }


    companion object {
        val log by logger()
        fun escape(name: String): String = name
    }

}