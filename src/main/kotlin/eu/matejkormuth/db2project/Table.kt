package eu.matejkormuth.db2project

import java.lang.reflect.Constructor
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import kotlin.RuntimeException

/**
 * Class for deriving information from entity classes. Used by DDL and runtime RDG operations.
 */
class Table<T : Entity>(klass: Class<T>) {
    val name: String = DDL.camelToSnakeCase(DDL.pluralize(klass.simpleName))

    /* remove kotlin Companion object from fields */
    private val fields = klass.declaredFields.filter { "Companion" !in it.name }

    private val fieldTypes = fields.map { it.type }.toTypedArray()
    private val satisfyingCtr = klass.getConstructor(*fieldTypes)

    val columns: Map<String, TableField> = fields
            .mapIndexed { idx, it -> TableField(it, satisfyingCtr.parameters[idx]) }
            .map { it.name to it }
            .toMap()
    private val idField = columns.values.firstOrNull { it.isId }
    private val instantier = Instantier<T>(satisfyingCtr, columns)

    val hasId = idField != null

    /**
     * Creates new query builder associated with this table.
     */
    fun queryBuilder(connection: Connection): QueryBuilder<T> = QueryBuilder(this, connection)

    /**
     * Class used to instantiate RDG objects from ResultSet objects.
     */
    internal class Instantier<T>(private val javaConstructor: Constructor<*>, columns: Map<String, TableField>) {
        data class CtrParam(val argIdx: Int, val columnName: String, val enumClass: Class<*>?, val tableField: TableField)

        private val paramsCount = javaConstructor.parameterCount
        private val ctrParams: MutableList<CtrParam> = mutableListOf()

        init {
            columns.values.forEachIndexed { index, tableField ->
                ctrParams.add(CtrParam(index, tableField.name, if (tableField.isEnum) tableField.type else null, tableField))
            }
        }

        fun new(resultSet: ResultSet, eagerLoadLazys: Boolean, aliases: MutableMap<TableField, String>?, thisAlias: String): T {
            val args = Array<Any?>(paramsCount) { null }
            ctrParams.forEach {

                args[it.argIdx] = resultSet.getObject((if (thisAlias != "") thisAlias + '_' else "") + it.columnName)

                /* convert enums represented by ints to enums */
                if (it.enumClass != null) {
                    args[it.argIdx] = enum(it.enumClass, args[it.argIdx] as Int)
                }

                /* convert timestamp to instant */
                if (args[it.argIdx] is Timestamp) {
                    val ts = args[it.argIdx] as Timestamp
                    args[it.argIdx] = Instant.ofEpochMilli(ts.time)
                }

                /* convert lazys represented by int to lazys */
                if (it.tableField.isReference) {

                    val lazy = if (args[it.argIdx] == null) Lazy.empty() else Lazy<Entity>(args[it.argIdx] as Int)
                    args[it.argIdx] = lazy

                    if (eagerLoadLazys && !lazy.isEmpty) {
                        if (aliases == null) throw RuntimeException("Must use aliases for referenced entities.")
                        val foreignTable = it.tableField.table
                        lazy.value = foreignTable.instantiate(
                                resultSet,
                                eagerLoadLazys = false,
                                aliases = aliases,
                                thisAlias = (aliases[it.tableField]
                                        ?: throw RuntimeException("No alias for ${it.tableField}"))
                        ) // todo: recursive multi-level
                    }
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

    fun instantiate(resultSet: ResultSet, eagerLoadLazys: Boolean, aliases: MutableMap<TableField, String>? = null, thisAlias: String? = null): T {
        try {
            return instantier.new(resultSet, eagerLoadLazys, aliases, if (aliases == null) "" else thisAlias ?: name)
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