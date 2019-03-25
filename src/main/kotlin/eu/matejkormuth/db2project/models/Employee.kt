package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

data class Employee(
        val id: Id = NewId,
        val name: String,
        val type: EmployeeType,
        val department: Lazy<Department>,
        @Maybe val rank: Int? = null
) : Entity() {

    fun assignCase(connectionAware: ConnectionAware, case: Case) {
        /* validate domain logic */
        if (type == EmployeeType.IKT_OFFICER)
            throw RuntimeException("IKT_OFFICER cannot be assigned to cases!")

        if (type == EmployeeType.INVESTIGATOR && case.caseType != CaseType.CRIME)
            throw RuntimeException("INVESTIGATOR cannot be assigned to cases other than CRIME!")

        /* perform the write */
        connectionAware.insertOne(AssignedEmployee(
                employee = Lazy(id),
                case = Lazy(case.id)
        ))
    }

    fun removeCase(connectionAware: ConnectionAware, case: Case) {
        val results = connectionAware.queryBuilder<AssignedEmployee>()
                .select()
                .eq("case_id", case.id)
                .and()
                .eq("employee_id", id)
                .fetchMultiple()

        if (results.count() == 0) throw RuntimeException("Specified assigment does not exists!")

        connectionAware.delete<AssignedEmployee>(results.first().id)
    }

}