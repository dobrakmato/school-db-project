package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.ui.*

object ApplicationUI {
    fun mainMenu(): Menu {
        val employeesMenu = Menu(listOf(
                MenuItem("List employees") { Scene.push(EmployeeUI.listEmployees()) },
                MenuItem("Create new employee") { Scene.push(EmployeeUI.createEmployee()) },
                MenuItem("Update employee information") { Scene.push(EmployeeUI.updateEmployee()) },
                MenuItem("Update cases of the employee") { Scene.push(EmployeeUI.updateCases()) },
                MenuItem("Delete an employee") { Scene.push(EmployeeUI.deleteEmployee()) }
        ), "[ Employees menu ]")

        val departmentsMenu = Menu(listOf(
                MenuItem("List departments") { Scene.push(DepartmentUI.listDepartments()) },
                MenuItem("Create new department"),
                MenuItem("Update department"),
                MenuItem("Delete department")
        ), "[ Departments menu ]")

        val casesMenu = Menu(listOf(
                MenuItem("List case information, people and crimes scenes"),
                MenuItem("Create new case"),
                MenuItem("Update existing case")
        ), "[ Cases menu ]")

        val crimeScenesMenu = Menu(listOf(
                MenuItem("List crimes scenes with cases and city districts"),
                MenuItem("Create new crime scene"),
                MenuItem("Update existing crime scene")
        ), "[ Crime scenes menu ]")

        val punishmentsMenu = Menu(listOf(
                MenuItem("List punishments"),
                MenuItem("Create new punishment")
        ), "[ Punishments menu ]")

        return Menu(listOf(
                MenuItem("Employees") { Scene.push(employeesMenu) },
                MenuItem("Departments") { Scene.push(departmentsMenu) },
                MenuItem("Cases") { Scene.push(casesMenu) },
                MenuItem("Crime scenes") { Scene.push(crimeScenesMenu) },
                MenuItem("Punishments") { Scene.push(punishmentsMenu) },
                MenuItem("☠️ Dangerous city districts") { Scene.push(dangerousCityDistricts()) },
                MenuItem("\uD83D\uDCC8 Cop of month") { Scene.push(copOfMonth()) }
        ), header = "[ ⭐⭐ POLICE DEPARTMENT - MENU ⭐⭐ ]", allowBack = false)
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

