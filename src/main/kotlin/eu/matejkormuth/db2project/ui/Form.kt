package eu.matejkormuth.db2project.ui

/**
 * UI component for rendering (and handling input) of form with predefined items to ask user for.
 * @see FormItem
 */
class Form(
        private val items: Iterable<FormItem>,
        private val header: String? = null,
        private val onComplete: (Answers) -> Unit
) : Drawable {

    private val answers = hashMapOf<FormItem, String>()

    override fun draw(ctx: ConsoleContext) {
        ctx.clear()

        header?.let { ctx.text(ctx.colors.brightBlue(header)) }
        ctx.text("")
        ctx.text(ctx.colors.gray("If item provides default value it is shown in brackets (eg: > Rank (1-10) [8]: )"))
        ctx.text(ctx.colors.gray("If you do not wish to change default value leave it empty and just press enter."))
        ctx.text("")
    }

    override fun handleInput(ctx: ConsoleContext) {
        items.forEach {
            ask(it, ctx)
        }
        handleAnswers()
    }

    /**
     * Asks user for specified form item.
     */
    private fun ask(it: FormItem, ctx: ConsoleContext) {
        var isValid = false
        while (!isValid) {
            if (it.defaultValue != null) {
                ctx.prompt("> ${it.text} [${it.defaultValue}]: ")
            } else {
                ctx.prompt("> ${it.text}: ")
            }
            val answer = ctx.readLine()
            isValid = it.validate(answer)
            if (!isValid) continue

            if (answer.isEmpty() && it.defaultValue != null) {
                answers[it] = it.defaultValue
            } else {
                answers[it] = answer
            }
        }
    }

    /**
     * Calls specified answers handler and then finishes this form.
     */
    private fun handleAnswers() {
        onComplete(Answers(answers))
        finish()
    }

    /**
     * Helper class to provide Map-like interface to answers.
     */
    class Answers(private val map: Map<FormItem, String>) {
        operator fun get(item: FormItem): String = map.getValue(item)
    }
}