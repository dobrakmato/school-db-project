package eu.matejkormuth.db2project.ui

data class MenuItem(val text: String, private val chooseAction: () -> Unit = {}) {
    fun onChoose() {
        chooseAction()
    }
}
