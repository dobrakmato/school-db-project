package eu.matejkormuth.db2project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias Id = Int
typealias Sql = String
const val NewId = -1334

inline fun <T> T.fluent(block: () -> Unit): T {
    block()
    return this
}

fun <R : Any> R.logger(): kotlin.Lazy<Logger> = lazy { LoggerFactory.getLogger(this::class.java.name) }