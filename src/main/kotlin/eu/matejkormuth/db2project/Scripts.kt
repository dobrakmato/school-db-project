package eu.matejkormuth.db2project

import com.github.javafaker.Faker
import eu.matejkormuth.db2project.models.*
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.math.asin

fun createTables() {
    transaction {
        DDL.createScript(
                AssignedEmployee::class.java,
                Case::class.java,
                Category::class.java,
                CityDistrict::class.java,
                Connection::class.java,
                CrimeScene::class.java,
                Department::class.java,
                Employee::class.java,
                Person::class.java,
                Punishment::class.java
        ).forEach { sql -> run(sql) }
        run("ALTER TABLE assigned_employees ADD CONSTRAINT uniq_case_empl UNIQUE (case_id, employee_id)")
        run("create index cases_created_at_closed_by_id_index on cases (created_at, closed_by_id);")
    }
}

inline fun <reified T : Entity> ConnectionAware.fillTable(count: Int, crossinline block: (Int) -> T) {
    var index = 0
    val seq = generateSequence { block(index++) }
    insertMultiple(seq.take(count).asIterable(), 100) // with 100 rows it runs very fast
}

var start: Long = 0

fun perf(msg: String) {
    val time = System.currentTimeMillis() - start
    println(" [$time ms] $msg")
}

fun fillTables() {
    val faker = Faker()

    val maxCityDistricts = 30
    val maxEmployees = 1000
    val maxDepartments = 50
    val maxCategories = 50
    val maxCrimeScenes = 5000
    val maxCases = 15000

    transaction {

        /* do foreign key checks at commit-time */
        run("SET CONSTRAINTS ALL DEFERRED")
        start = System.currentTimeMillis()

        perf("Creating $maxCityDistricts CityDistrict objects...")
        fillTable(maxCityDistricts) {
            CityDistrict(
                    name = faker.address().cityName()
            )
        }

        val categories = mutableListOf<Category>()

        perf("Creating $maxCategories Category objects...")
        fillTable(maxCategories) {
            val category = Category(
                    id = it + 1,
                    name = faker.pokemon().location(),
                    fineAmount = faker.random().nextInt(1000)
            )
            categories.add(category)
            category
        }

        perf("Creating $maxCrimeScenes CrimeScene objects...")
        fillTable(maxCrimeScenes) {
            CrimeScene(
                    name = faker.address().fullAddress(),
                    cityDistrict = Lazy(faker.random().nextInt(maxCityDistricts) + 1)
            )
        }

        /* ------------------------------------------------- */

        perf("Creating $maxDepartments Department objects...")
        fillTable(maxDepartments) {
            Department(
                    name = faker.commerce().department(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees) + 1)
            )
        }

        val canWorkOnMisdemeanors = mutableListOf<Employee>()
        val canWorkOnCrimes = mutableListOf<Employee>()
        val canWorkOnProtectiveActions = mutableListOf<Employee>()
        val canBeHeadEmployee = mutableListOf<Employee>()
        val canCreateArrestWarrants = mutableListOf<Employee>()
        val canCreateFines = mutableListOf<Employee>()
        val canConfirmConnections = mutableListOf<Employee>()

        perf("Creating $maxEmployees Employee objects...")
        fillTable(maxEmployees) {
            val type = EmployeeType.values().random()
            val employee = Employee(
                    id = it + 1,
                    name = faker.name().fullName(),
                    type = type,
                    department = Lazy(faker.random().nextInt(maxDepartments) + 1),
                    rank = if (type == EmployeeType.POLICEMAN) faker.random().nextInt(20) else null
            )

            /* add generated employee to groups */
            if (type.canWorkOnMisdemeanors()) canWorkOnMisdemeanors.add(employee)
            if (type.canWorkOnCrimes()) canWorkOnCrimes.add(employee)
            if (type.canWorkOnProtectiveActions()) canWorkOnProtectiveActions.add(employee)
            if (type.canBeCaseHeadEmployee()) canBeHeadEmployee.add(employee)
            if (type.canCreateArrestWarrant()) canCreateArrestWarrants.add(employee)
            if (type.canCreateFine()) canCreateFines.add(employee)
            if (type.canConfirmConnection()) canConfirmConnections.add(employee)

            employee
        }


        /* -------------------------------- */

        val cases = mutableListOf<Case>()
        val punishments = mutableListOf<Punishment>()
        val people = mutableListOf<Person>()
        val connections = mutableListOf<Connection>()
        val assignedEmployees = mutableListOf<AssignedEmployee>()

        perf("Generating Cases, Connections, AssignedEmployees, Persons, Punishments data...")
        repeat(maxCases) {
            val caseType = CaseType.values().random()
            val shouldBeClosed = faker.random().nextBoolean()

            val caseCategory = categories[faker.random().nextInt(maxCategories)]

            val case = Case(
                    id = it + 1,
                    description = faker.book().title(),
                    headEmployee = Lazy(canBeHeadEmployee.random().id),
                    caseType = caseType,
                    closedBy = if (shouldBeClosed) Lazy(faker.random().nextInt(maxEmployees) + 1) else null,
                    caseCategory = Lazy(caseCategory.id),
                    protectiveActionPlace = if (caseType == CaseType.PROTECTIVE_ACTION) Lazy(faker.random().nextInt(maxCrimeScenes) + 1) else null,
                    createdAt = faker.date().past(365, TimeUnit.DAYS).toInstant()
            )
            cases.add(case)

            /* generate connections if not protective action */
            if (caseType != CaseType.PROTECTIVE_ACTION) {
                repeat(faker.random().nextInt(5, 50)) {
                    val personType = PersonType.values().random()
                    val mustBeConfirmed = shouldBeClosed && personType != PersonType.WITNESS
                    val mustBePunished = mustBeConfirmed /* seems to be the same */

                    val person = Person(
                            id = people.size + 1,
                            name = faker.funnyName().name(),
                            personType = personType
                    )
                    people.add(person)

                    val isConfirmed = if (mustBeConfirmed) true else faker.random().nextBoolean()

                    val connection = Connection(
                            case = Lazy(case.id),
                            person = Lazy(person.id),
                            crimeScene = Lazy(faker.random().nextInt(maxCrimeScenes) + 1),
                            confirmedBy = if (isConfirmed) Lazy(canConfirmConnections.random().id) else null,
                            confirmedAt = if (isConfirmed) faker.date().past(365, TimeUnit.DAYS).toInstant() else null
                    )

                    connections.add(connection)

                    /* generate punishments */
                    if (mustBePunished) {
                        val punishmentType = when (caseType) {
                            CaseType.MISDEMEANOR -> PunishmentType.FINE
                            CaseType.CRIME -> PunishmentType.ARREST_WARRANT
                            else -> throw RuntimeException("Should not happen")
                        }

                        punishments.add(Punishment(
                                punished = Lazy(person.id),
                                punishmentType = punishmentType,
                                fineAmount = if (punishmentType == PunishmentType.ARREST_WARRANT) null else caseCategory.fineAmount
                        ))
                    }
                }
            }

            /* assign employees to case */
            val dedupe = mutableSetOf<Employee>()
            repeat(faker.random().nextInt(3, 30)) {
                val assigned = when (caseType) {
                    CaseType.MISDEMEANOR -> canWorkOnMisdemeanors.random()
                    CaseType.CRIME -> canWorkOnCrimes.random()
                    CaseType.PROTECTIVE_ACTION -> canWorkOnProtectiveActions.random()
                }

                if (assigned !in dedupe) {
                    assignedEmployees.add(AssignedEmployee(
                            case = Lazy(case.id),
                            employee = Lazy(assigned.id)
                    ))
                    dedupe.add(assigned)
                }
            }
        }


        perf("Inserting ${connections.size} Connection objects...")
        insertMultiple(connections, maxRowsAtOnce = 100)

        perf("Inserting ${people.size} Person objects...")
        insertMultiple(people, maxRowsAtOnce = 100)

        perf("Inserting ${punishments.size} Punishment objects...")
        insertMultiple(punishments, maxRowsAtOnce = 100)

        perf("Inserting ${assignedEmployees.size} AssignedEmployee objects...")
        insertMultiple(assignedEmployees, maxRowsAtOnce = 100)

        perf("Inserting ${cases.size} Case objects...")
        insertMultiple(cases, maxRowsAtOnce = 100)
    }
}