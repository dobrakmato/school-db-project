package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.Menu
import eu.matejkormuth.db2project.ui.MenuItem
import eu.matejkormuth.db2project.ui.Scene

fun main() {
    Application.run()
}

object Application {
    fun run() {
        //Database.initialize()
        //Scene.clear()

        val mainMenu = Menu(listOf(
                MenuItem("Employees"),
                MenuItem("Departments"),
                MenuItem("Cases"),
                MenuItem("Crime scenes"),
                MenuItem("Punishments"),
                MenuItem("☠️ Dangerous city districts"),
                MenuItem("\uD83D\uDCC8 Cop of month")
        ), header = "[ ⭐⭐ POLICE DEPARTMENT - MENU ⭐⭐ ]")

        //Scene.content = mainMenu

        println(DDL.createScript(Case::class.java, Category::class.java, CityDistrict::class.java,
                Connection::class.java, CrimeScene::class.java, Department::class.java,
                Employee::class.java, Person::class.java, Punishment::class.java).joinToString(separator = "\n\n"))
    }
}