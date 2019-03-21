package eu.matejkormuth.db2project.ui

import kotlin.text.StringBuilder

class DataTable<Row>(
        private val rows: Iterable<Row>,
        private val header: Iterable<String>,
        private val padding: Int = 6,
        private val rowRender: (Row) -> Iterable<String>
) : Drawable {

    private val columnsWidths: IntArray

    init {
        val itr = header.iterator()
        columnsWidths = IntArray(header.count()) { itr.next().length + padding }
    }

    override fun handleInput(ctx: ConsoleContext) {
        ctx.readLine()
    }

    override fun draw(ctx: ConsoleContext) {
        val renderedRows = rows.map {
            val row = rowRender(it)
            row.forEachIndexed { idx, cell ->
                columnsWidths[idx] = Math.max(columnsWidths[idx], cell.length + padding)
            }
            row
        }

        tableHeader(ctx)
        printSeparator(ctx)
        renderedRows.forEach { ctx.text(transformToRow(it)) }
        printSeparator(ctx)
    }

    private fun tableHeader(ctx: ConsoleContext) = ctx.text(transformToRow(header))

    private fun printSeparator(ctx: ConsoleContext) = ctx
            .text(columnsWidths.joinToString("+") { "-".repeat(it) })

    private fun transformToRow(items: Iterable<String>) = items
            .mapIndexed { idx, it -> padCenter(it, columnsWidths[idx]) }
            .joinToString("|")

    private fun padCenter(str: String, width: Int, char: Char = ' '): String {
        val whitespace = width - str.length
        val left = Math.floor(whitespace / 2.0).toInt()
        val right = Math.ceil(whitespace / 2.0).toInt()

        val builder = StringBuilder()
        repeat(left) { builder.append(char) }
        builder.append(str)
        repeat(right) { builder.append(char) }

        return builder.toString()
    }

}