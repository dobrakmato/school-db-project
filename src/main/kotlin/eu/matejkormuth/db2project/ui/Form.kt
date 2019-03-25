package eu.matejkormuth.db2project.ui

class Form(
        private val items: Iterable<FormItem>,
        private val header: String? = null,
        private val onComplete: (Answers) -> Unit
) : Drawable {

    private val answers = hashMapOf<FormItem, String>()

    override fun draw(ctx: ConsoleContext) {
        header?.let { ctx.text(header) }
    }

    override fun handleInput(ctx: ConsoleContext) {
        items.forEach {
            ask(it, ctx)
        }
        finish()
    }

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
            answers[it] = answer
        }
    }

    private fun finish() {
        onComplete(Answers(answers))
    }

    class Answers(private val map: Map<FormItem, String>) {
        operator fun get(item: FormItem): String = map.getValue(item)
    }
}