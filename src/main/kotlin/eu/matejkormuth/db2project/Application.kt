package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.screens.ApplicationUI
import eu.matejkormuth.db2project.ui.Scene
import kotlin.system.measureTimeMillis

fun main() {
    /* Initialize connection to DB server. */
    Database.initialize()

    /* Create database schema and data. */
    // println("Tables created in " + measureTimeMillis { createTables() } + " ms")
    // println("Tables seeded in " + measureTimeMillis { fillTables() } + " ms")

    /* Bootstrap the UI */
    Scene.push(ApplicationUI.mainMenu())
    Scene.loop()
}
