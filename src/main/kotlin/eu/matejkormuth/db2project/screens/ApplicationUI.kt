package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.models.CopOfMonth
import eu.matejkormuth.db2project.models.DangerousCityDistricts
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
                MenuItem("Create new department") { Scene.push(DepartmentUI.createDepartment()) },
                MenuItem("Update department") { Scene.push(DepartmentUI.updateDepartment()) },
                MenuItem("Delete department") { Scene.push(DepartmentUI.deleteDepartment()) }
        ), "[ Departments menu ]")

        val casesMenu = Menu(listOf(
                MenuItem("List case information, people and crimes scenes") { Scene.push(CaseUI.listCase()) },
                MenuItem("Create new case") { Scene.push(CaseUI.createCase()) },
                MenuItem("Close case (domain operation)") { Scene.push(CaseUI.closeCase()) },
                MenuItem("Auto-assign employees to case (domain operation)") { Scene.push(CaseUI.autoAssignEmployeesToCase()) },
                MenuItem("Update existing case") { Scene.push(CaseUI.updateCase()) }
        ), "[ Cases menu ]")

        val crimeScenesMenu = Menu(listOf(
                MenuItem("List crimes scenes with cases and city districts") { Scene.push(CrimeSceneUI.listCrimeScenes()) },
                MenuItem("Create new crime scene") { Scene.push(CrimeSceneUI.createCrimeScene()) },
                MenuItem("Update existing crime scene") { Scene.push(CrimeSceneUI.updateCrimeScene()) }
        ), "[ Crime scenes menu ]")

        val punishmentsMenu = Menu(listOf(
                MenuItem("List punishments") { Scene.push(PunishmentUI.listPunishments()) },
                MenuItem("Create new punishment (domain operation)") { Scene.push(PunishmentUI.createPunishment()) }
        ), "[ Punishments menu ]")

        return Menu(listOf(
                MenuItem("Employees") { Scene.push(employeesMenu) },
                MenuItem("Departments") { Scene.push(departmentsMenu) },
                MenuItem("Cases") { Scene.push(casesMenu) },
                MenuItem("Crime scenes") { Scene.push(crimeScenesMenu) },
                MenuItem("Punishments") { Scene.push(punishmentsMenu) },
                MenuItem("Promotion (domain operation)") { Scene.push(EmployeeUI.promotion()) },
                MenuItem("Transfer employees (domain operation)") { Scene.push(EmployeeUI.transferEmployees()) },
                MenuItem("☠️ Dangerous city districts (statistic)") { Scene.push(dangerousCityDistricts()) },
                MenuItem("\uD83D\uDCC8 Cop of month (statistic)") { Scene.push(copOfMonth()) }
        ), header = "[ ⭐⭐ POLICE DEPARTMENT - MENU ⭐⭐ ]", allowBack = false)
    }

    /* Statistics */

    private fun dangerousCityDistricts(): Drawable {
        val rows = DangerousCityDistricts.getAllRows()
        return DataTable(rows, listOf("Year", "Quarter", "Month", "#", "District", "Cases", "Misdemeanors", "Crimes")) {
            listOf(
                    it.year,
                    it.quarter,
                    it.month,
                    it.position.toString(),
                    it.district,
                    it.casesCount.toString(),
                    it.type0Count.toString(),
                    it.type1Count.toString()
            )
        }
    }

    private fun copOfMonth(): Drawable {
        val rows = CopOfMonth.getAllRows()
        return DataTable(rows, listOf("Month", "#", "Closed by", "Closed count", "#", "Confirmed by", "Confirmed count")) {
            listOf(
                    it.month.toString(),
                    it.closedPosition.toString(),
                    it.closedBy,
                    it.closedCount.toString(),
                    it.confirmedPosition.toString(),
                    it.confirmedBy,
                    it.confirmedCount.toString()
            )
        }
    }
}

