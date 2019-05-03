package eu.matejkormuth.db2project.ui

import eu.matejkormuth.db2project.findAllReferenced
import eu.matejkormuth.db2project.findOne
import eu.matejkormuth.db2project.getOrNull
import eu.matejkormuth.db2project.models.AssignedEmployee
import eu.matejkormuth.db2project.models.Case
import eu.matejkormuth.db2project.models.CaseType
import eu.matejkormuth.db2project.models.Connection
import eu.matejkormuth.db2project.transaction

object CasePrinter {
    fun printCase(caseId: Int, ctx: ConsoleContext) = transaction {

        val case = findOne<Case>(caseId, eagerLoad = true)
                ?: throw RuntimeException("Case does not exists!")
        val assignedEmployees = findAllReferenced<AssignedEmployee>(case.id, "case_id", true)
        val connections = findAllReferenced<Connection>(case.id, "case_id", true)

        /* case details */
        ctx.text("+----------------------------------+")
        ctx.text("|           CASE DETAILS           |")
        ctx.text("+----------------------------------+")
        ctx.text("")

        ctx.text("ID: ${case.id}")
        ctx.text("Description: ${case.description}")
        ctx.text("Category: ${case.caseCategory.getOrNull()!!.name}")
        ctx.text("Type: ${case.caseType}")
        ctx.text("Created at: ${case.createdAt}")
        ctx.text("Head employee: ${case.headEmployee.getOrNull()!!.name}")
        if (case.closedBy != null && !case.closedBy.isEmpty) {
            ctx.text("Closed by: ${case.closedBy.getOrNull()!!.name}")
        }
        if (case.caseType == CaseType.PROTECTIVE_ACTION) {
            ctx.text("Crime scene: ${case.protectiveActionPlace?.id}")
        }
        ctx.text("")

        /* assigned employees */
        DataTable(assignedEmployees, listOf("ID", "Assigned employee name")) {
            listOf(it.id.toString(), it.employee.getOrNull()!!.name)
        }.draw(ctx)

        /* connections with persons */
        DataTable(connections, listOf("Connection ID", "Person name", "Person type", "Crime scene", "Confirmed By", "Confirmed at")) {
            listOf(
                    it.id.toString(),
                    it.person.getOrNull()!!.name,
                    it.person.getOrNull()!!.personType.toString(),
                    it.crimeScene.getOrNull()!!.name,
                    it.confirmedBy?.getOrNull()?.name ?: "-",
                    it.confirmedAt?.toString() ?: "-"
            )
        }.draw(ctx)
    }
}