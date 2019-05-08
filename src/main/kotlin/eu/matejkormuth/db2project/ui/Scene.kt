package eu.matejkormuth.db2project.ui

import eu.matejkormuth.db2project.logger
import java.util.*

/**
 * Main UI handling class. Provides layering of different UI components in stack fashion.
 */
object Scene {

    private val stack = ArrayDeque<Drawable>(16)
    private val ctx = ConsoleContext()

    /**
     * Main UI loop method.
     */
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

    /**
     * Pushes new drawable onto the stack.
     */
    fun push(drawable: Drawable) {
        log.debug("Pushed drawable $drawable")
        stack.push(drawable)
    }

    /**
     * Pops specified drawable if it is the topmost drawable on the stack.
     */
    fun pop(drawable: Drawable) {
        if (stack.peek() == drawable) {
            stack.pop()
            log.debug("Popped drawable $drawable")
        }
    }

    /**
     * Replaces current top with specified - essentially pop() then push() in one call
     */
    fun replace(drawable: Drawable) {
        log.debug("Replacing current top with $drawable")
        stack.pop()
        push(drawable)
    }

    private val log by logger()
}