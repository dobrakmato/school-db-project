package eu.matejkormuth.db2project.ui

import eu.matejkormuth.db2project.logger
import java.util.*

object Scene {

    private val stack = ArrayDeque<Drawable>(16)
    private val ctx = ConsoleContext()

    fun loop() {
        /* Perform first clear */
        ctx.clear()

        log.debug("Entering loop()")
        while (true) {
            val top = stack.peek()
            log.debug("top() = $top")

            /* check for problems */
            if (top == null) {
                stack.add(Text(ctx.colors.red("Scene: No drawable content"), true))
                continue
            }

            top.draw(ctx)
            top.handleInput(ctx)
        }
    }

    fun push(drawable: Drawable) {
        log.debug("Pushed drawable $drawable")
        stack.push(drawable)
    }

    fun pop(drawable: Drawable) {
        if (stack.peek() == drawable) {
            stack.pop()
            log.debug("Popped drawable $drawable")
        }
    }

    /* replace current top with specified - essentially pop() then push() in one call */
    fun replace(drawable: Drawable) {
        log.debug("Replacing current top with $drawable")
        stack.pop()
        push(drawable)
    }

    private val log by logger()
}