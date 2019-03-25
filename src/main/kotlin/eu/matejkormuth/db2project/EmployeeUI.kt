package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.AssignedEmployee
import eu.matejkormuth.db2project.models.Employee
import eu.matejkormuth.db2project.models.EmployeeType
import eu.matejkormuth.db2project.ui.*

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
                    insertOne(Employee(
                            name = it[name],
                            type = EmployeeType.valueOf(it[type].toUpperCase()),
                            rank = it[rank].toIntOrNull(10),
                            department = Lazy(it[department].toInt(10))
                    ))
                } catch (ex: Exception) {
                    println("Insert operation failed! ${ex.message}")
                }
            }
        }
    }

    fun updateEmployee(): Drawable {
        return DirectControl { ctx ->
            val employeeId = FormItem.requiredId("Employee ID (to update)")
            Form(listOf(employeeId)) {
                transaction {
                    val employee = findOne<Employee>(it[employeeId].toInt(10))
                            ?: return@Form println("Specified employee does not exists!")

                    val allowedEmployeeTypes = EmployeeType.values().map { it.toString() }

                    val name = FormItem.required("Full name", employee.name)
                    val type = FormItem.oneOf("Employee type (one of ${allowedEmployeeTypes.joinToString(", ")})", employee.type.toString(), allowedEmployeeTypes)
                    val rank = FormItem("Rank (1 - 10, leave blank if non-applicable)", employee.rank?.toString())
                    val department = FormItem.requiredId("Department ID", employee.department.id.toString())

                    val updateForm = Form(listOf(name, type, rank, department), "[ Form - Update existing employee ]") {
                        updateOne(employee.copy(
                                name = it[name],
                                type = EmployeeType.valueOf(it[type].toUpperCase()),
                                rank = it[rank].toIntOrNull(),
                                department = Lazy(it[department].toInt())
                        ))
                    }

                    updateForm.draw(ctx)
                    updateForm.handleInput(ctx)
                }
            }.handleInput(ctx)
        }
    }

    fun updateCases(): Drawable {
        return Menu(listOf(
                MenuItem("Assign case to employee") { Scene.content = addCaseToEmployee() },
                MenuItem("Dissociate case from employee") { Scene.content = removeCaseFromEmployee() }
        ), "[ Menu - Update cases of employee ]")
    }

    private fun addCaseToEmployee(): Drawable {
        val caseId = FormItem.requiredId("Case ID")
        val employeeId = FormItem.requiredId("Employee ID")
        return Form(listOf(caseId, employeeId), "[Form  - Add case to employee]") {
            transaction {
                try {
                    insertOne(AssignedEmployee(
                            employee = Lazy(it[employeeId].toInt(10)),
                            case = Lazy(it[caseId].toInt(10))
                    ))
                } catch (ex: Exception) {
                    println("Cannot add specified employee to specified case. One of referenced entities probably " +
                            "does not exists or the employee is already assigned to specified case.")
                }
            }
        }
    }

    private fun removeCaseFromEmployee(): Drawable {
        val caseId = FormItem.requiredId("Case ID")
        val employeeId = FormItem.requiredId("Employee ID")
        return Form(listOf(caseId, employeeId), "[Form  - Remove case from employee]") {
            transaction {
                val results = queryBuilder<AssignedEmployee>()
                        .select()
                        .eq("case_id", it[caseId])
                        .eq("employee_id", it[employeeId])
                        .fetchMultiple()

                if (results.count() == 0) {
                    return@Form println("Specified assigment does not exists!")
                }

                delete<AssignedEmployee>(results.first().id)
            }
        }
    }

    fun deleteEmployee(): Drawable {
        val id = FormItem.requiredId("Employee ID")
        return Form(listOf(id), "[ Form - Delete existing employee ]") {
            try {
                transaction { delete<Employee>(it[id].toInt(10)) }
            } catch (ex: Exception) {
                println("Cannot delete employee it either does not exists or has stuff tied to him.")
            }
        }
    }
}