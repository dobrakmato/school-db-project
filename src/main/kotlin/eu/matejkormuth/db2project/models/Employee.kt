package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*

/**
 * This class represents Employee and is used to generate employees table.
 */
data class Employee(
        val id: Id = NewId,
        val name: String,
        val type: EmployeeType,
        val department: Lazy<Department>,
        @Maybe val rank: Int? = null
) : Entity() {

    companion object {

        /**
         * Assigns case specified by id to employee specified by id.
         *
         * @param caseId id of case
         * @param employeeId id of employee
         * @throws RuntimeException when operation fails
         */
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
                try {
                    return insertOne(AssignedEmployee(
                            employee = Lazy(employee.id),
                            case = Lazy(case.id)
                    ))
                } finally {
                    commit()
                }
            }
        }

        /**
         * Withholds case specified by id to employee specified by id.
         *
         * @param caseId id of case
         * @param employeeId id of employee
         * @throws RuntimeException when operation fails
         */
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
                commit()
            }
        }

        /**
         * Finds employees who have small number of cases assigned to them.
         *
         * @param caseId id of case
         * @param exceptEmployees id of employees to not include in result
         * @param ctx connection context
         * @throws RuntimeException when operation fails
         */
        fun findBoredEmployees(caseId: Int, exceptEmployees: Iterable<Int>, ctx: ConnectionAware): Iterable<Employee> {
            val except = exceptEmployees + listOf(0)

            val q = loadQuery("/bored_employees.sql")
                    .replaceFirst("?", caseId.toString())
                    .replaceFirst("?", except.joinToString(","))
            return ctx.runQuery(q) { rs ->
                rs.use {
                    it.map {
                        Employee(
                                getInt("id"),
                                getString("name"),
                                EmployeeType.values()[getInt("type")],
                                Lazy(getInt("department_id")),
                                getInt("rank")
                        )
                    }
                }
            }
        }
    }


}