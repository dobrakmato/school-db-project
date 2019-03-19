package eu.matejkormuth.db2project.ui

class Form(
        private val items: Iterable<FormItem>,
        private val header: String? = null,
        private val onComplete: (Map<FormItem, String>) -> Unit
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
        ctx.prompt("> ${it.text} [${it.defaultValue}]: ")
        answers[it] = ctx.readLine()
    }

    private fun finish() {
        onComplete(answers)
    }
}