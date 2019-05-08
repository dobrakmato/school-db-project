package eu.matejkormuth.db2project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Postgres ID.
 */
typealias Id = Int

/**
 * Sql string type.
 */
typealias Sql = String

/**
 * Constant used to new rows.
 */
const val NewId = -1334

/**
 * Helper for building fluent interfaces.
 */
inline fun <T> T.fluent(block: () -> Unit): T {
    block()
    return this
}

/**
 * Creates a logger instance.
 */
fun <R : Any> R.logger(): kotlin.Lazy<Logger> = lazy { LoggerFactory.getLogger(this::class.java.name) }