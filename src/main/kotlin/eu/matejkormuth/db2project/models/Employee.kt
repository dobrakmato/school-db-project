package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Employee(
        val id: Id = NewId,
        val name: String,
        val type: EmployeeType,
        val department: Lazy<Department>,
        @Maybe val rank: Int? = null
) : Entity() {

    companion object {
        fun assignCase(employeeId: Id, caseId: Id): Lazy<AssignedEmployee> {
            transaction {
                val employee = findOne<Employee>(employeeId, forUpdate = true)
                        ?: throw RuntimeException("Employee does not exists!")
                val case = findOne<Case>(caseId, forUpdate = true) ?: throw RuntimeException("Case does not exists!")

                /* validate domain logic */
                if (employee.type == EmployeeType.IKT_OFFICER)
                    throw RuntimeException("IKT_OFFICER cannot be assigned to cases!")

                if (employee.type == EmployeeType.INVESTIGATOR && case.caseType != CaseType.CRIME)
                    throw RuntimeException("INVESTIGATOR cannot be assigned to cases other than CRIME!")

                /* perform the write */
                return insertOne(AssignedEmployee(
                        employee = Lazy(employee.id),
                        case = Lazy(case.id)
                ))
            }
        }

        fun removeCase(employeeId: Id, caseId: Id) {
            transaction {
                val employee = findOne<Employee>(employeeId, forUpdate = true)
                        ?: throw RuntimeException("Employee does not exists!")
                val case = findOne<Case>(caseId, forUpdate = true) ?: throw RuntimeException("Case does not exists!")
                val results = queryBuilder<AssignedEmployee>()
                        .select()
                        .eq("case_id", case.id)
                        .and()
                        .eq("employee_id", employee.id)
                        .fetchMultiple()

                if (results.count() == 0) throw RuntimeException("Specified assigment does not exists!")

                delete<AssignedEmployee>(results.first().id)
            }
        }
    }


}