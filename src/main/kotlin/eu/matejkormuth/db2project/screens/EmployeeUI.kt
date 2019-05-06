package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.*
import eu.matejkormuth.db2project.models.Employee
import eu.matejkormuth.db2project.models.EmployeeType
import eu.matejkormuth.db2project.ui.*
import java.sql.SQLException

object EmployeeUI {

    fun listEmployees(): Drawable {
        val employees = transaction { findAll<Employee>(eagerLoad = true) }

        return DataTable(employees, listOf("ID", "Name", "Type", "Department")) {
            listOf(it.id.toString(), it.name, it.type.toString(), it.department.getOrNull()!!.name)
        }
    }

    fun createEmployee(): Drawable {
        val allowedEmployeeTypes = EmployeeType.values().map { it.toString() }

        val name = FormItem.required("Full name")
        val type = FormItem.oneOf("Employee type (one of ${allowedEmployeeTypes.joinToString(", ")})", possible = allowedEmployeeTypes)
        val rank = FormItem("Rank (1 - 10, leave blank if non-applicable)")
        val department = FormItem.requiredId("Department ID")

        return Form(listOf(name, type, rank, department), "[ Form - Create new employee ]") {
            transaction {
                try {
                    val employee = insertOne(Employee(
                            name = it[name],
                            type = EmployeeType.valueOf(it[type].toUpperCase()),
                            rank = it[rank].toIntOrNull(10),
                            department = Lazy(it[department].toInt(10))
                    ))
                    Scene.replace(Success("Employee inserted successfully ID: ${employee.id}"))
                } catch (ex: Exception) {
                    rollback()
                    Scene.replace(Error("Insert operation failed!"))
                }
            }
        }
    }

    fun updateEmployee(): Drawable {
        return DirectControl { ctx ->
            val employeeId = FormItem.requiredId("Employee ID (to update)")
            Form(listOf(employeeId)) {
                transaction {
                    val employee = findOne<Employee>(it[employeeId].toInt(10), forUpdate = true)
                            ?: return@Form Scene.replace(Error("Specified employee does not exists!"))

                    val allowedEmployeeTypes = EmployeeType.values().map { it.toString() }

                    val name = FormItem("Full name", employee.name)
                    val type = FormItem.oneOfOptional("Employee type (one of ${allowedEmployeeTypes.joinToString(", ")})", employee.type.toString(), allowedEmployeeTypes)
                    val rank = FormItem("Rank (1 - 10, leave blank if non-applicable)", employee.rank?.toString())
                    val department = FormItem("Department ID", employee.department.id.toString())

                    val updateForm = Form(listOf(name, type, rank, department), "[ Form - Update existing employee ]") {
                        updateOne(employee.copy(
                                name = it[name],
                                type = EmployeeType.valueOf(it[type].toUpperCase()),
                                rank = it[rank].toIntOrNull(),
                                department = Lazy(it[department].toInt())
                        ))
                        Scene.replace(Success("Employee updated!"))
                    }

                    updateForm.draw(ctx)
                    updateForm.handleInput(ctx)
                }
            }.handleInput(ctx)
        }
    }

    fun updateCases(): Drawable {
        return Menu(listOf(
                MenuItem("Assign case to employee") { Scene.push(addCaseToEmployee()) },
                MenuItem("Dissociate case from employee") { Scene.push(removeCaseFromEmployee()) }
        ), "[ Menu - Update cases of employee ]")
    }

    fun addCaseToEmployee(): Drawable {
        val caseId = FormItem.requiredId("Case ID")
        val employeeId = FormItem.requiredId("Employee ID")
        return Form(listOf(caseId, employeeId), "[Form  - Add case to employee]") {
            try {
                Employee.assignCase(it[employeeId].toInt(), it[caseId].toInt())
                Scene.replace(Success("Case assigned!"))
            } catch (ex: Exception) {
                if (ex is SQLException) {
                    Scene.replace(Error("Cannot add specified employee to specified case. Detail: Already assigned."))
                } else {
                    Scene.replace(Error("Cannot add specified employee to specified case. Detail: ${ex.message}"))
                }
            }
        }
    }

    fun removeCaseFromEmployee(): Drawable {
        val caseId = FormItem.requiredId("Case ID")
        val employeeId = FormItem.requiredId("Employee ID")
        return Form(listOf(caseId, employeeId), "[Form  - Remove case from employee]") {
            try {
                Employee.removeCase(it[employeeId].toInt(), it[caseId].toInt())
                Scene.replace(Success("Case removed!"))
            } catch (ex: Exception) {
                Scene.replace(Error("Cannot remove specified employee from specified case. Detail: ${ex.message}"))
            }
        }
    }

    fun deleteEmployee(): Drawable {
        val id = FormItem.requiredId("Employee ID")
        return Form(listOf(id), "[ Form - Delete existing employee ]") {
            try {
                transaction { delete<Employee>(it[id].toInt(10)) }
                Scene.replace(Success("Success!"))
            } catch (ex: Exception) {
                Scene.replace(Error("Cannot delete employee it either does not exists or has stuff tied to him."))
            }
        }
    }

    fun promotion(): Drawable = transaction {
        try {
            val updated = runUpdate(loadQuery("/promotion.sql"))
            commit()
            Success("Success! Promoted $updated employees.")
        } catch (ex: Exception) {
            Error("Sorry, something went wrong.")
        }
    }


    fun transferEmployees(): Drawable = transaction {
        try {
            val updated = runUpdate(loadQuery("/move_employees.sql"))
            commit()
            Success("Success! Moved $updated employees from over-populated case to under-populatedcase.")
        } catch (ex: Exception) {
            Error("Sorry, something went wrong.")
        }
    }
}