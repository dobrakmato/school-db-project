package eu.matejkormuth.db2project

import java.lang.StringBuilder
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClassifier


object DDL {

    private fun pluralize(str: String): String {
        if (str.endsWith("y")) {
            return "${str.trimEnd('y')}ies"
        }
        return "${str}s"
    }

    private fun camelToSnakeCase(str: String): String {
        val snakeCase = StringBuilder()

        for ((index, c) in str.withIndex()) {
            snakeCase.append(c.toLowerCase())

            if (index + 2 < str.length && str[index + 1].isUpperCase() && str[index + 2].isLowerCase()) {
                snakeCase.append('_')
            }
        }

        return snakeCase.toString()
    }

    fun createScript(vararg tables: Class<*>): List<Sql> {
        fun isNullable(it: Field): Boolean = it.isAnnotationPresent(Maybe::class.java)

        fun createSqlType(it: Field): String {
            val nullType = { i: Field -> if (isNullable(i)) "" else " NOT NULL" }
            if (it.type.isEnum) return "INTEGER${nullType(it)}"

            return when (it.type) {
                Boolean::class.java -> "TINYINT${nullType(it)}"
                String::class.java -> "VARCHAR(255)${nullType(it)}"
                Id::class.java -> "SERIAL"
                Int::class.java -> "INTEGER${nullType(it)}"
                Integer::class.java -> "INTEGER${nullType(it)}"
                Lazy::class.java -> "INTEGER REFERENCES ${camelToSnakeCase(pluralize(((it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>).simpleName))} id"
                else -> throw UnsupportedOperationException("Type ${it.type} unrecognized!")
            }
        }

        fun isReference(it: Field) = it.type == Lazy::class.java

        fun createTable(table: Class<*>): Sql {
            val tableName = camelToSnakeCase(pluralize(table.simpleName))
            var sql = "CREATE TABLE $tableName (\n"
            table.declaredFields.forEach {
                val name = if (isReference(it)) camelToSnakeCase(it.name) + "_id" else camelToSnakeCase(it.name)
                val type = createSqlType(it)

                sql += "\t $name $type, \n"
            }
            return "$sql)"
        }

        return tables.map { createTable(it) }.toList()
    }

}