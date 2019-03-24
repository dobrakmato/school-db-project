package eu.matejkormuth.db2project

import com.github.javafaker.Faker
import eu.matejkormuth.db2project.models.*
import java.util.concurrent.TimeUnit

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
    }
}

inline fun <reified T : Entity> ConnectionAware.fillTable(count: Int, crossinline block: (Int) -> T) {
    var index = 0
    val seq = generateSequence { block(index++) }
    insertMultiple(seq.take(count).asIterable(), 100) // with 100 rows it runs very fast
}

fun fillTables() {
    val faker = Faker()

    val maxCityDistricts = 30
    val maxPersons = 10000
    val maxConnections = 8000
    val maxEmployees = 500
    val maxAssignedEmployees = 700
    val maxDepartments = 30
    val maxCategories = 30
    val maxCrimeScenes = 800
    val maxCases = 4000

    val confirmed = BooleanArray(maxPersons) { Math.random() > 0.5 }

    transaction {
        run("SET CONSTRAINTS ALL DEFERRED")

        println("Creating CityDistrict objects...")
        fillTable(maxCityDistricts) {
            CityDistrict(
                    name = faker.address().cityName()
            )
        }

        println("Creating Category objects...")
        fillTable(maxCategories) {
            Category(
                    name = faker.pokemon().location()
            )
        }

        println("Creating Department objects...")
        fillTable(maxDepartments) {
            Department(
                    name = faker.commerce().department(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees) + 1)
            )
        }

        println("Creating Employee objects...")
        fillTable(maxEmployees) {
            val type = EmployeeType.values().random()
            Employee(
                    name = faker.name().fullName(),
                    type = type,
                    department = Lazy(faker.random().nextInt(maxDepartments) + 1),
                    rank = if (type == EmployeeType.POLICEMAN) faker.random().nextInt(20) else null
            )
        }


        val punishments = mutableListOf<Punishment>()

        println("Creating Person objects...")
        fillTable(maxPersons) {
            val punishment: Lazy<Punishment>? = null
            val personType = PersonType.values().random()

            if (personType == PersonType.SUSPECT && confirmed[it]) {
                val punishmentType = PunishmentType.values().random()
                val maxFineAmount = 2000
                punishments.add(Punishment(
                        punishmentType = punishmentType,
                        fineAmount = if (punishmentType == PunishmentType.FINE) {
                            faker.random().nextInt(maxFineAmount)
                        } else null
                ))
            }

            Person(
                    name = faker.funnyName().name(),
                    personType = personType,
                    punishment = punishment
            )
        }

        println("Creating Punishment objects...")
        insertMultiple(punishments)

        println("Creating CrimeScene objects...")
        fillTable(maxCrimeScenes) {
            CrimeScene(
                    name = faker.address().fullAddress(),
                    cityDistrict = Lazy(faker.random().nextInt(maxCityDistricts) + 1)
            )
        }

        println("Creating Case objects...")
        fillTable(maxCases) {
            val caseType = CaseType.values().random()
            Case(
                    description = faker.book().title(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees) + 1),
                    caseType = caseType,
                    closedBy = if (faker.random().nextDouble() > 0.3) Lazy(faker.random().nextInt(maxEmployees) + 1) else null,
                    caseCategory = Lazy(faker.random().nextInt(maxCategories) + 1),
                    protectiveActionPlace = if (caseType == CaseType.PROTECTIVE_ACTION) Lazy(faker.random().nextInt(maxCrimeScenes) + 1) else null,
                    createdAt = faker.date().past(365, TimeUnit.DAYS).toInstant()
            )
        }

        println("Creating AssignedEmployee objects...")
        fillTable(maxAssignedEmployees) {
            AssignedEmployee(
                    case = Lazy(faker.random().nextInt(maxCases) + 1),
                    employee = Lazy(faker.random().nextInt(maxEmployees) + 1)
            )
        }

        println("Creating Connection objects...")
        fillTable(maxConnections) {
            val personId = faker.random().nextInt(maxPersons) + 1
            Connection(
                    case = Lazy(faker.random().nextInt(maxCases) + 1),
                    person = Lazy(personId),
                    crimeScene = Lazy(faker.random().nextInt(maxCrimeScenes) + 1),
                    confirmedBy = if (confirmed[personId - 1]) Lazy(faker.random().nextInt(maxEmployees) + 1) else null,
                    confirmedAt = if (confirmed[personId - 1]) faker.date().past(365, TimeUnit.DAYS).toInstant() else null
            )
        }
    }
}