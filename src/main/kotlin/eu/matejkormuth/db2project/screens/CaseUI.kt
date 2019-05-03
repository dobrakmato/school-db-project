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
                    CasePrinter.printCase(it[caseId].toInt(), ctx)
                } catch (ex: Exception) {
                    Scene.replace(Error("Case does not exists or there is some other problem."))
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

        return Form(listOf(description, headEmployeeId, placeId, caseType, categoryId), "[ Form - Create case ]") {
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
        val confirmerId = FormItem.requiredId("Who is confirming the connection? (ID)")
        val connectionId = FormItem.requiredId("Connection ID")
        return Form(listOf(confirmerId, connectionId), "[ Form - Confirm connection ]") {
            try {
                Connection.confirm(it[connectionId].toInt(), it[confirmerId].toInt())
                Scene.replace(Success("Connection confirmed"))

            } catch (ex: Exception) {
                Scene.replace(Error("Cannot confirm connection: $ex"))
            }
        }
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
