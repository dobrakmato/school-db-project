package eu.matejkormuth.db2project.ui

import java.lang.Exception
import java.lang.RuntimeException
import java.util.*

class ConsoleContext {

    private val stdinReader = Scanner(System.`in`)

    fun clear() {
        repeat(30) { println() }
    }

    fun text(msg: String, end: String = "\n") {
        print(msg + end)
    }

    fun prompt(msg: String) = text(msg, end = "")

    fun readInt(minValue: Int = Int.MIN_VALUE, maxValue: Int = Int.MAX_VALUE): Optional<Int> {
        return try {
            val value = Integer.parseInt(stdinReader.nextLine())
            if (value < minValue || value > maxValue) throw RuntimeException("Constraints not met")
            Optional.of(value)
        } catch (ex: Exception) {
            Optional.empty()
        }
    }
}