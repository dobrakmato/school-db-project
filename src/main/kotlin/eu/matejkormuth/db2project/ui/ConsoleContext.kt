package eu.matejkormuth.db2project.ui

import com.github.ajalt.mordant.TermColors
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*

/**
 * Class that abstracts stdout/stdin to "console" object.
 */
class ConsoleContext {

    private val stdinReader = Scanner(System.`in`)
    val colors = TermColors()

    /**
     * Clears the console.
     */
    fun clear() {
        repeat(30) { println() }
    }

    /**
     * Prints text message to console ended by end parameter (defaults to newline).
     * @param msg message to print
     * @param end ender of message
     */
    fun text(msg: String, end: String = "\n") {
        print(msg + end)
    }

    /**
     * Asks user for line.
     */
    fun prompt(msg: String) = text(msg, end = "")

    /**
     * Asks user for int with specified bounds.
     */
    fun readInt(minValue: Int = Int.MIN_VALUE, maxValue: Int = Int.MAX_VALUE): Optional<Int> {
        return try {
            val value = Integer.parseInt(stdinReader.nextLine())
            if (value < minValue || value >= maxValue) throw RuntimeException("Constraints not met")
            Optional.of(value)
        } catch (ex: Exception) {
            Optional.empty()
        }
    }

    /**
     * Reads line from console.
     */
    fun readLine(): String = stdinReader.nextLine()
}