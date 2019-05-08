package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*
import java.time.Instant

/**
 * This class represents Case and is used to generate cases table.
 */
data class Case(
        val id: Id = NewId,
        val description: String,
        val headEmployee: Lazy<Employee>,
        val caseType: CaseType,
        val caseCategory: Lazy<Category>,
        @Maybe val closedBy: Lazy<Employee>? = null,
        @Maybe val protectiveActionPlace: Lazy<CrimeScene>? = null /* for protective action */,
        @Index val createdAt: Instant = Instant.now()
) : Entity() {
    companion object {

        /**
         * Tries to close case by it's id and id of employee who is closing it.
         *
         * @param caseId id of case
         * @param closedById id of closer
         * @throws RuntimeException when operation fails
         */
        fun close(caseId: Int, closedById: Int) = transaction {
            val case = findOne<Case>(caseId, forUpdate = true) ?: throw RuntimeException("Case not found!")
            val closedBy = findOne<Employee>(closedById, forUpdate = true)
                    ?: throw RuntimeException("Employee not found!")

            if (case.closedBy != null && !case.closedBy.isEmpty) throw RuntimeException("Case already closed!")

            val caseCategory = retrieve(case.caseCategory)!!
            val connections = findAllReferenced<Connection>(caseId, "case_id").map {
                it.person.get(this.connection)
                it
            }

            /* ensure all are confirmed */
            val victimsAndSuspectsConfirmed = connections
                    .filter { it.person.getOrNull()!!.personType == PersonType.SUSPECT || it.person.getOrNull()!!.personType == PersonType.VICTIM }
                    .all { Lazy.notEmpty(it.confirmedBy) }

            if (!victimsAndSuspectsConfirmed) throw RuntimeException("Not all victims and suspects are confirmed!")

            /* punish unpunished suspects */
            connections.forEach {
                val punishment = findReferenced<Punishment>(it.person.id, "punished_id")
                if (it.person.value!!.personType == PersonType.SUSPECT && punishment == null) {
                    insertOne(Punishment.create(it.person.value!!, case, caseCategory))
                }
            }

            /* update the case */
            updateOne(case.copy(
                    closedBy = Lazy(closedBy.id)
            ))

        }

        /**
         * Automatically assigns specified count of employees to specified case.`
         *
         * @param caseId id of case
         * @param count amount of employees to assign
         * @throws RuntimeException when operation fails
         */
        fun autoAssign(caseId: Int, count: Int): Iterable<Employee> = transaction {
            val case = findOne<Case>(caseId, forUpdate = true) ?: throw RuntimeException("Case not found!")
            val alreadyAssigned = findAllReferenced<AssignedEmployee>(case.id, "case_id")

            /* find employees with least works to do that are not assigned to this case */
            var employees = Employee.findBoredEmployees(caseId, alreadyAssigned.map { it.employee.id }, this).filter {
                if (case.caseType == CaseType.CRIME) return@filter true
                if (it.type == EmployeeType.INSPECTOR || it.type == EmployeeType.POLICEMAN) return@filter true
                return@filter false
            }

            /* assign all of them to this case */
            employees.take(count).forEach {
                insertOne(AssignedEmployee(
                        case = Lazy(caseId),
                        employee = Lazy(it.id)
                ))
            }

            /* add current head to list of candidates */
            employees = employees + retrieve(case.headEmployee)!!
            /* find head employee for the case */
            val inspector = employees.find { it.type == EmployeeType.INSPECTOR }
            val policeman = employees.filter { it.type == EmployeeType.POLICEMAN }.maxBy { it.rank ?: 0 }
            var headEmployee = inspector ?: policeman

            /* if there is no viable option for head employee */
            if (headEmployee == null) {
                headEmployee = find<Employee>()
                        .eq("type", EmployeeType.INSPECTOR.ordinal)
                        .limit(1)
                        .fetchOne()!!

                insertOne(AssignedEmployee(
                        case = Lazy(caseId),
                        employee = Lazy(headEmployee.id)
                ))
            }

            /* update the case */
            updateOne(case.copy(
                    headEmployee = Lazy(headEmployee.id)
            ))

            commit()
            return employees
        }

    }
}