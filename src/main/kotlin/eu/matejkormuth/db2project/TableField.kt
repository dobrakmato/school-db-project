package eu.matejkormuth.db2project

import java.lang.reflect.Field
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.time.Instant

data class TableField(private val it: Field, private val parameter: Parameter) {
    private val intyTypes = listOf(Int::class.java, Integer::class.java)

    val type = it.type
    val isReference = it.type == Lazy::class.java
    val name = DDL.camelToSnakeCase(it.name) + if (isReference) "_id" else ""
    val codeName = it.name
    val isId = it.type == Id::class.java
    val isEnum = it.type.isEnum
    val isNullable = parameter.isAnnotationPresent(Maybe::class.java)
    val genericType by lazy { it.genericType }

    @Suppress("UNCHECKED_CAST")
    val table by lazy { Database.tableFor(((genericType as ParameterizedType).actualTypeArguments[0] as Class<Entity>)) }

    val isStringy: Boolean = it.type == String::class.java
    val isBoolean: Boolean = it.type == Boolean::class.java
    val isInty: Boolean = isEnum || isReference || intyTypes.contains(it.type)

    init {
        /* allow access to field */
        if (!it.isAccessible) it.isAccessible = true
    }

    fun <T> idFor(entity: T): Id = it.getInt(entity)

    fun <T> booleanFor(entity: T): Boolean? = it.getBoolean(entity)

    fun <T> instantFor(row: T): Instant? = it.get(row) as? Instant

    fun <T> valueFor(row: T): String? {
        return if (isEnum) {
            (it.get(row) as Enum<*>).ordinal.toString()
        } else {
            val toString = { it.get(row)?.toString() }
            when (it.type) {
                String::class.java, Int::class.java, Integer::class.java -> toString()
                Lazy::class.java -> (it.get(row) as Lazy<*>?)?.id?.toString()
                else -> throw UnsupportedOperationException("Cannot make string of ${it.type}!")
            }
        }
    }
}