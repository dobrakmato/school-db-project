package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.ui.*
import java.util.*

object ApplicationUI {
    fun mainMenu() {
        val employees = Menu(listOf(
                MenuItem("List employees"),
                MenuItem("Create new employee"),
                MenuItem("Update employee information"),
                MenuItem("Update cases of the employee"),
                MenuItem("Delete an employee")
        ))

        val departments = Menu(listOf(
                MenuItem("List departments"),
                MenuItem("Create new department"),
                MenuItem("Update department"),
                MenuItem("Delete department")
        ))

        val cases = Menu(listOf(
                MenuItem("List case information, people and crimes scenes"),
                MenuItem("Create new case"),
                MenuItem("Update existing case")
        ))

        val crimeScenes = Menu(listOf(
                MenuItem("List crimes scenes with cases and city districts"),
                MenuItem("Create new crime scene"),
                MenuItem("Update existing crime scene")
        ))

        val punishments = Menu(listOf(
                MenuItem("List punishments"),
                MenuItem("Create new punishment")
        ))

        val mainMenu = Menu(listOf(
                MenuItem("Employees") { Scene.content = employees },
                MenuItem("Departments") { Scene.content = departments },
                MenuItem("Cases") { Scene.content = cases },
                MenuItem("Crime scenes") { Scene.content = crimeScenes },
                MenuItem("Punishments") { Scene.content = punishments },
                MenuItem("☠️ Dangerous city districts") { Scene.content = dangerousCityDistricts() },
                MenuItem("\uD83D\uDCC8 Cop of month") { Scene.content = copOfMonth() }
        ), header = "[ ⭐⭐ POLICE DEPARTMENT - MENU ⭐⭐ ]")

        Scene.content = mainMenu
    }

    private fun dangerousCityDistricts(): Drawable {
        return Text("dangerousCityDistricts()")
    }

    private fun copOfMonth(): Drawable {
        data class Tmp(val month: Int, val maxCases: String, val maxConnections: String)

        val rows = transaction {
            runQuery(loadQuery("/cop_of_month.sql")) { rs ->
                rs.use { it.map { Tmp(getInt(1), getString(2), getString(3)) } }
            }
        }
        return DataTable(rows, listOf("Month", "Max closed cases", "Max confirmed connections")) {
            listOf(it.month.toString(), it.maxCases, it.maxConnections)
        }
    }
}

