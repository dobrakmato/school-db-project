package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.*
import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.*
import java.sql.SQLException
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
                MenuItem("Add connection/person to case") { Scene.push(addConnectionToCase()) },
                MenuItem("Confirm connection") { Scene.push(confirmConnection()) }
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
        val allowedConnectionTypes = CaseType.values().map { it.toString() }

        val caseId = FormItem.requiredId("Case ID")
        val crimeSceneId = FormItem.requiredId("Crime scene ID")
        val connectionType = FormItem.oneOf("Case type (one of ${allowedConnectionTypes.joinToString(", ")})", possible = allowedConnectionTypes)

        val personName = FormItem.required("Connection type")
        return Form(listOf(caseId, crimeSceneId, connectionType, personName), "[ Form - Confirm connection ]") {
            try {
                transaction {
                    val case = findOne<Case>(it[caseId].toInt()) ?: throw RuntimeException("Case not found!")
                    val crimeScene = findOne<CrimeScene>(it[crimeSceneId].toInt())
                            ?: throw RuntimeException("Crime scene not found!")
                    val person = insertOne(Person(
                            name = it[personName],
                            personType = PersonType.valueOf(it[connectionType])
                    ))
                    insertOne(Connection(
                            case = Lazy(case.id),
                            crimeScene = Lazy(crimeScene.id),
                            confirmedAt = null,
                            confirmedBy = null,
                            person = person
                    ))
                }

                Scene.replace(Success("Connection created!"))
            } catch (ex: Exception) {
                if (ex is SQLException) {
                    Scene.replace(Error("Connection creation failed!"))
                } else {
                    Scene.replace(Error("Cannot create connection: $ex"))
                }
            }
        }
    }

    fun closeCase(): Drawable {
        val closerId = FormItem.requiredId("Who is closing the case? (ID)")
        val caseId = FormItem.requiredId("Case ID")
        return Form(listOf(closerId, caseId), "[ Form - Close case ]") {
            try {
                Case.close(it[caseId].toInt(), it[closerId].toInt())
                Scene.replace(Success("Case closed"))

            } catch (ex: Exception) {
                Scene.replace(Error("Cannot close case: $ex"))
            }
        }
    }

    fun autoAssignEmployeesToCase(): Drawable {
        val caseId = FormItem.requiredId("Case ID")
        return Form(listOf(caseId), "[ Form - Auto assign employees ]") {
            try {
                val employees = Case.autoAssign(it[caseId].toInt())
                Scene.replace(Success("Employees (${employees.joinToString(", ") { e -> e.name }}) auto assigned!"))

            } catch (ex: Exception) {
                Scene.replace(Error("Cannot close case: $ex"))
            }
        }
    }

}
