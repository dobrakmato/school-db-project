package eu.matejkormuth.db2project

import java.lang.reflect.Constructor
import java.sql.Connection
import java.sql.ResultSet

class Table<T : Entity>(klass: Class<T>) {
    val name: String = DDL.camelToSnakeCase(DDL.pluralize(klass.simpleName))

    private val fields = klass.declaredFields.map { it.type }.toTypedArray()
    private val satisfyingCtr = klass.getConstructor(*fields)

    val columns: Map<String, TableField> = klass.declaredFields
            .mapIndexed { idx, it -> TableField(it, satisfyingCtr.parameters[idx]) }
            .map { it.name to it }
            .toMap()
    private val instantier = Instantier<T>(satisfyingCtr, columns)

    fun queryBuilder(connection: Connection): QueryBuilder<T> = QueryBuilder(this, connection)

    internal class Instantier<T>(private val javaConstructor: Constructor<*>, columns: Map<String, TableField>) {
        data class CtrParam(val argIdx: Int, val columnName: String, val enumClass: Class<*>?)

        private val paramsCount = javaConstructor.parameterCount
        private val ctrParams: MutableList<CtrParam> = mutableListOf()

        init {
            columns.values.forEachIndexed { index, tableField ->
                ctrParams.add(CtrParam(index, tableField.name, if (tableField.isEnum) tableField.type else null))
            }
        }

        fun new(resultSet: ResultSet): T {
            val args = Array<Any?>(paramsCount) { null }
            ctrParams.forEach {
                args[it.argIdx] = resultSet.getObject(it.columnName)

                if (it.enumClass != null) {
                    args[it.argIdx] = enum(it.enumClass, args[it.argIdx] as Int)
                }
            }
            return callJavaCtr(args)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T> enum(enumClass: Class<*>, ordinal: Int): T {
            val constants = (enumClass.enumConstants as Array<Enum<*>>)
            return constants[ordinal] as T
        }

        @Suppress("UNCHECKED_CAST")
        private fun callJavaCtr(args: Array<Any?>): T {
            return javaConstructor.newInstance(*args) as T
        }
    }


    fun instantiate(resultSet: ResultSet): T = instantier.new(resultSet)

    companion object {
        val log by logger()
    }
}