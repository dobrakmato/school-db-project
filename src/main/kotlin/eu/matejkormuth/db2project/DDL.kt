package eu.matejkormuth.db2project

import java.lang.StringBuilder
import java.lang.reflect.ParameterizedType
import java.time.Instant

object DDL {

    fun pluralize(str: String): String {
        if (str.endsWith("y")) {
            return "${str.trimEnd('y')}ies"
        }
        return "${str}s"
    }

    fun camelToSnakeCase(str: String): String {
        val snakeCase = StringBuilder()

        for ((index, c) in str.withIndex()) {
            snakeCase.append(c.toLowerCase())

            if (index + 2 < str.length && str[index + 1].isUpperCase() && str[index + 2].isLowerCase()) {
                snakeCase.append('_')
            }
        }

        return snakeCase.toString()
    }

    fun createScript(vararg tables: Class<out Entity>): List<Sql> {
        fun createSqlType(it: TableField): String {
            val nullType = if (it.isNullable) "" else " NOT NULL"
            if (it.isEnum) return "INTEGER$nullType"

            return when (it.type) {
                Boolean::class.java -> "BOOLEAN"
                String::class.java -> "VARCHAR(255)"
                Instant::class.java -> "TIMESTAMP"
                Id::class.java -> "SERIAL PRIMARY KEY"
                Int::class.java, Integer::class.java, Lazy::class.java -> "INTEGER"
                else -> throw UnsupportedOperationException("Type ${it.type} unrecognized!")
            } + nullType
        }

        fun createForeignKeys(table: Table<out Entity>): Iterable<String> {
            return table.columns.values.filter { it.isReference }.map {
                val refType = ((it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>)
                val refTypeName = camelToSnakeCase(pluralize(refType.simpleName))
                "ALTER TABLE ${table.name} ADD FOREIGN KEY (${it.name}) REFERENCES $refTypeName (id) DEFERRABLE"
            }
        }

        fun createTableQueries(table: Table<out Entity>): Sql {
            var sql = "DROP TABLE IF EXISTS ${table.name} CASCADE; CREATE TABLE IF NOT EXISTS ${table.name} (\n"
            table.columns.values.forEach {
                val name = if (it.isReference) camelToSnakeCase(it.name) else camelToSnakeCase(it.name)
                val type = createSqlType(it)

                sql += "\t $name $type, \n"
            }
            sql = sql.trim('\n', ' ', ',') + '\n'
            return "$sql);"
        }

        val create = tables.map { Database.tableFor(it) }.map { createTableQueries(it) }.toList()
        val fk = tables.map { Database.tableFor(it) }.flatMap { createForeignKeys(it) }.toList()

        return create + fk
    }

}