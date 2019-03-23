package eu.matejkormuth.db2project

import java.lang.RuntimeException
import java.lang.reflect.Constructor
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

class Table<T : Entity>(klass: Class<T>) {
    val name: String = DDL.camelToSnakeCase(DDL.pluralize(klass.simpleName))

    private val fields = klass.declaredFields.map { it.type }.toTypedArray()
    private val satisfyingCtr = klass.getConstructor(*fields)

    val columns: Map<String, TableField> = klass.declaredFields
            .mapIndexed { idx, it -> TableField(it, satisfyingCtr.parameters[idx]) }
            .map { it.name to it }
            .toMap()
    private val idField = columns.values.firstOrNull { it.isId }
    private val instantier = Instantier<T>(satisfyingCtr, columns)

    val hasId = idField != null

    fun queryBuilder(connection: Connection): QueryBuilder<T> = QueryBuilder(this, connection)

    internal class Instantier<T>(private val javaConstructor: Constructor<*>, columns: Map<String, TableField>) {
        data class CtrParam(val argIdx: Int, val columnName: String, val enumClass: Class<*>?, val isReference: Boolean)

        private val paramsCount = javaConstructor.parameterCount
        private val ctrParams: MutableList<CtrParam> = mutableListOf()

        init {
            columns.values.forEachIndexed { index, tableField ->
                ctrParams.add(CtrParam(index, tableField.name, if (tableField.isEnum) tableField.type else null, tableField.isReference))
            }
        }

        fun new(resultSet: ResultSet): T {
            val args = Array<Any?>(paramsCount) { null }
            ctrParams.forEach {
                args[it.argIdx] = resultSet.getObject(it.columnName)

                /* convert enums represented by ints to enums */
                if (it.enumClass != null) {
                    args[it.argIdx] = enum(it.enumClass, args[it.argIdx] as Int)
                }

                /* convert lazys represented by int to lazys */
                if (it.isReference) {
                    args[it.argIdx] = Lazy<Entity>(it.argIdx)
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
            try {
                return javaConstructor.newInstance(*args) as T
            } catch (ex: Exception) {
                log.error("Expected arg types: ${Arrays.toString(javaConstructor.parameterTypes)}")
                log.error("Actual args: ${Arrays.toString(args)}")
                log.error("Actual arg types: ${(args.joinToString(", ") { it?.javaClass?.simpleName ?: "null" })}")
                throw ex
            }
        }
    }


    fun instantiate(resultSet: ResultSet): T {
        try {
            return instantier.new(resultSet)
        } catch (ex: Exception) {
            log.error("Cannot instantiate '$name' entity!")
            throw ex
        }
    }

    fun extractId(entity: T): Id = idField?.idFor(entity)
            ?: throw  UnsupportedOperationException("Cannot get Id for non-id entity.")

    companion object {
        val log by logger()
    }
}