package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.*
import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.*
import java.time.Instant

object CaseUI {
    fun listCase(): Drawable {
        val caseId = FormItem.requiredId("Case ID")
        return DirectControl { ctx ->
            val caseIdForm = Form(listOf(caseId)) {
                try {
                    transaction {
                        val case = findOne<Case>(it[caseId].toInt(), eagerLoad = true)
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
                } catch (ex: Exception) {
                    Scene.replace(Error("Cannot view details of case: $ex"))
                }
            }
            caseIdForm.draw(ctx)
            caseIdForm.handleInput(ctx)
            ctx.readLine() /* wait for enter */
        }
    }

    fun createCase(): Drawable {
        val allowedCaseTypes = CaseType.values().map { it.toString() }

        val description = FormItem.required("Description")
        val headEmployeeId = FormItem.requiredId("Head Employee ID")
        val placeId = FormItem.requiredId("Place ID (for crime and misdemeanor leave empty)")
        val caseType = FormItem.oneOf("Case type (one of ${allowedCaseTypes.joinToString(", ")})", possible = allowedCaseTypes)
        val categoryId = FormItem.requiredId("Category ID")

        return Form(listOf()) {
            try {
                transaction {
                    val employee = findOne<Employee>(it[headEmployeeId].toInt())
                            ?: throw RuntimeException("Cannot find specified employee!")
                    val category = findOne<Category>(it[categoryId].toInt())
                            ?: throw RuntimeException("Cannot find specified category!")

                    if (!employee.type.canBeCaseHeadEmployee()) throw RuntimeException("Specified employeed cannot be head employee of case!")

                    val type = CaseType.valueOf(it[caseType])

                    val place: CrimeScene?
                    if (type == CaseType.PROTECTIVE_ACTION) {
                        place = findOne(it[placeId].toInt())
                                ?: throw RuntimeException("Cannot find specified place (crime scene)!")
                    } else {
                        place = null
                    }

                    val case = insertOne(Case(
                            description = it[description],
                            headEmployee = Lazy(employee.id),
                            caseCategory = Lazy(category.id),
                            caseType = type,
                            protectiveActionPlace = if (type == CaseType.PROTECTIVE_ACTION) Lazy(place?.id
                                    ?: 0) else null,
                            createdAt = Instant.now()
                    ))
                    Scene.replace(Success("Case created (${case.id})"))
                }
            } catch (ex: Exception) {
                Scene.replace(Error("Cannot create case: $ex"))
            }
        }
    }

    fun updateCase(): Drawable {
        return Menu(listOf(
                MenuItem("Assign case to employee") { Scene.push(EmployeeUI.addCaseToEmployee()) },
                MenuItem("Dissociate case from employee") { Scene.push(EmployeeUI.removeCaseFromEmployee()) },
                MenuItem("\uD83D\uDED1 Add connection/person to case") { Scene.push(addConnectionToCase()) },
                MenuItem("\uD83D\uDED1 Confirm connection") { Scene.push(confirmConnection()) }
        ), "[ Menu - Update case ]")
    }

    private fun confirmConnection(): Drawable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun addConnectionToCase(): Drawable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun closeCase(): Drawable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun autoAssignEmployeesToCase(): Drawable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
