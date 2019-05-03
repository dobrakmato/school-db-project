package eu.matejkormuth.db2project.models

import eu.matejkormuth.db2project.*
import java.time.Instant

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

        fun close(caseId: Int, closedById: Int) = transaction {
            val case = findOne<Case>(caseId, forUpdate = true) ?: throw RuntimeException("Case not found!")
            val caseCategory = retrieve(case.caseCategory)!!
            val connections = findAllReferenced<Connection>(caseId, "case_id").map {
                retrieve(it.person)
                it
            }


            /* ensure all are confirmed */
            val victimsAndSuspectsConfirmed = connections.all {
                if (it.person.value!!.personType in listOf(PersonType.SUSPECT, PersonType.VICTIM)) {
                    return@all it.confirmedAt != null
                }
                return@all true
            }

            if (!victimsAndSuspectsConfirmed) throw RuntimeException("Not all victims and suspects are confirmed!")

            /* punish unpunished suspects */
            connections.forEach {
                if (it.person.value!!.personType == PersonType.SUSPECT && it.confirmedAt == null) {
                    insertOne(Punishment.create(it.person.value!!, case, caseCategory))
                }
            }

            /* update the case */
            updateOne(case.copy(
                    closedBy = Lazy(closedById)
            ))

        }

        fun autoAssign(caseId: Int): Iterable<Employee> = transaction {
            val case = findOne<Case>(caseId, forUpdate = true) ?: throw RuntimeException("Case not found!")

            /* find employees with least works to do that are not assigned to this case */
            var employees = Employee.findBoredEmployees(caseId, this).filter {
                if (case.caseType == CaseType.CRIME) return@filter true
                if (it.type == EmployeeType.INSPECTOR || it.type == EmployeeType.POLICEMAN) return@filter true
                return@filter false
            }

            /* assign all of them to this case */
            employees.forEach {
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


            return employees
        }

    }
}