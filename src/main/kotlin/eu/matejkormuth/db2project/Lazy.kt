package eu.matejkormuth.db2project

data class Lazy<T>(
        val id: Int
) {
    fun retrieve(): T {
        return null!!
    }
}