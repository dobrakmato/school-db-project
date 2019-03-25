package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.Department
import eu.matejkormuth.db2project.ui.*

object ApplicationUI {
    fun mainMenu() {
        val employees = Menu(listOf(
                MenuItem("List employees") { Scene.content = EmployeeUI.listEmployees() },
                MenuItem("Create new employee") { Scene.content = EmployeeUI.createEmployee() },
                MenuItem("Update employee information") { Scene.content = EmployeeUI.updateEmployee() },
                MenuItem("Update cases of the employee") { Scene.content = EmployeeUI.updateCases() },
                MenuItem("Delete an employee") { Scene.content = EmployeeUI.deleteEmployee() }
        ))

        val departments = Menu(listOf(
                MenuItem("List departments") { Scene.content = listDepartments() },
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

    /* CRUD */

    private fun listDepartments(): Drawable {
        val departments = transaction { findAll<Department>(eagerLoad = true) }

        return DataTable(departments, listOf("ID", "Name", "Head employee")) {
            listOf(it.id.toString(), it.name, it.headEmployee.getOrNull()!!.name)
        }
    }

    /* Domain operations */

    /* Statistics */

    private fun dangerousCityDistricts(): Drawable {
        return Text("dangerousCityDistricts()")
    }

    private fun copOfMonth(): Drawable {
        val rows = transaction {
            runQuery(loadQuery("/cop_of_month.sql")) { rs ->
                rs.use { it.map { Triple(getInt(1), getString(2), getString(3)) } }
            }
        }

        return DataTable(rows, listOf("Month", "Max closed cases", "Max confirmed connections")) {
            listOf(it.first.toString(), it.second, it.third)
        }
    }
}

