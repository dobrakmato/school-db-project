package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.*
import eu.matejkormuth.db2project.models.Department
import eu.matejkormuth.db2project.models.Employee
import eu.matejkormuth.db2project.ui.*

object DepartmentUI {
    fun listDepartments(): Drawable {
        val departments = transaction { findAll<Department>(eagerLoad = true) }

        return DataTable(departments, listOf("ID", "Name", "Head employee")) {
            listOf(it.id.toString(), it.name, it.headEmployee.getOrNull()!!.name)
        }
    }

    fun createDepartment(): Drawable {
        val name = FormItem.required("Department name")
        val headEmployeeId = FormItem.requiredId("Head Employee ID")

        return Form(listOf(name, headEmployeeId), "[ Form - Create new department ]") {
            try {
                transaction {
                    val employee = findOne<Department>(it[headEmployeeId].toInt())
                            ?: throw RuntimeException("Specified employee does not exists!")
                    val department = insertOne(Department(
                            name = it[name],
                            headEmployee = Lazy(employee.id)
                    ))
                    Scene.replace(Success("Department created (${department.id})!"))
                }
            } catch (ex: Exception) {
                Scene.replace(Error("Insert operation failed! ${ex.message}"))
            }

        }
    }

    fun updateDepartment(): Drawable {
        return DirectControl { ctx ->
            val departmentId = FormItem.requiredId("Department ID (to update)")
            Form(listOf(departmentId)) {
                transaction {
                    val department = findOne<Department>(it[departmentId].toInt(10), forUpdate = true)
                            ?: return@Form Scene.replace(Error("Specified department does not exists!"))

                    val name = FormItem("Department name", department.name)
                    val headEmployeeId = FormItem("Head Employee ID", department.headEmployee.id.toString())

                    val updateForm = Form(listOf(name, headEmployeeId), "[ Form - Update existing department ]") {
                        val employee = findOne<Employee>(it[headEmployeeId].toInt())

                        if (employee == null) {
                            Scene.replace(Error("Specified employee does not exists!"))
                            return@Form
                        }

                        updateOne(department.copy(
                                name = it[name],
                                headEmployee = Lazy(employee.id)
                        ))
                        Scene.replace(Success("Department updated!"))
                    }

                    updateForm.draw(ctx)
                    updateForm.handleInput(ctx)
                }
            }.handleInput(ctx)
        }
    }

    fun deleteDepartment(): Drawable {
        val id = FormItem.requiredId("Department ID")
        return Form(listOf(id), "[ Form - Delete existing department ]") {
            try {
                transaction {
                    val department = findOne<Department>(it[id].toInt(10), forUpdate = true)
                            ?: throw RuntimeException("Department does not exists!")
                    delete<Department>(department.id)
                }
                Scene.replace(Success("Success!"))
            } catch (ex: Exception) {
                Scene.replace(Error("Cannot delete department: it either does not exists or has stuff tied to it."))
            }
        }
    }
}