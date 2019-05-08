package eu.matejkormuth.db2project.ui

/**
 * Menu item of menu.
 * @see Menu
 */
data class MenuItem(val text: String, private val chooseAction: () -> Unit = {}) {
    /**
     * Called when this menu item is chosen.
     */
    fun onChoose() {
        chooseAction()
    }
}
