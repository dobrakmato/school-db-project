package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.Department
import eu.matejkormuth.db2project.ui.DataTable
import eu.matejkormuth.db2project.ui.Drawable
import eu.matejkormuth.db2project.ui.Error

object DepartmentUI {
    fun listDepartments(): Drawable {
        val departments = transaction { findAll<Department>(eagerLoad = true) }

        return DataTable(departments, listOf("ID", "Name", "Head employee")) {
            listOf(it.id.toString(), it.name, it.headEmployee.getOrNull()!!.name)
        }
    }

    fun createDepartment(): Drawable {
        return Error("Not yet implemented")
    }

    fun updateDepartment(): Drawable {
        return Error("Not yet implemented")
    }

    fun deleteDepartment(): Drawable {
        return Error("Not yet implemented")
    }
}