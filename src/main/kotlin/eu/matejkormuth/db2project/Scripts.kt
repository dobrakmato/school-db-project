package eu.matejkormuth.db2project

import com.github.javafaker.Faker
import eu.matejkormuth.db2project.models.*

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

fun fillTables() {
    val faker = Faker()

    val maxCityDistricts = 30
    val maxPersons = 1000
    val maxConnections = 1000
    val maxEmployees = 250
    val maxAssignedEmployees = 400
    val maxDepartments = 30
    val maxCategories = 30
    val maxCrimeScenes = 300
    val maxCases = 80

    val confirmed = BooleanArray(maxPersons) { Math.random() > 0.5 }

    transaction {
        run("SET CONSTRAINTS ALL DEFERRED")

        println("Creating CityDistrict objects...")
        repeat(maxCityDistricts) {
            insertOne(CityDistrict(
                    name = faker.address().cityName()
            ))
        }

        println("Creating Category objects...")
        repeat(maxCategories) {
            insertOne(Category(
                    name = faker.pokemon().location()
            ))
        }

        println("Creating Department objects...")
        repeat(maxDepartments) {
            insertOne(Department(
                    name = faker.commerce().department(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees) + 1)
            ))
        }

        println("Creating Employee objects...")
        repeat(maxEmployees) {
            val type = EmployeeType.values().random()
            insertOne(Employee(
                    name = faker.name().fullName(),
                    type = type,
                    department = Lazy(faker.random().nextInt(maxDepartments) + 1),
                    rank = if (type == EmployeeType.POLICEMAN) faker.random().nextInt(20) else null
            ))
        }

        println("Creating Person objects...")
        repeat(maxPersons) {
            var punishment: Lazy<Punishment>? = null
            val personType = PersonType.values().random()

            if (personType == PersonType.SUSPECT && confirmed[it]) {
                val punishmentType = PunishmentType.values().random()
                val maxFineAmount = 2000
                punishment = insertOne(Punishment(
                        punishmentType = punishmentType,
                        fineAmount = if (punishmentType == PunishmentType.FINE) {
                            faker.random().nextInt(maxFineAmount)
                        } else null
                ))
            }

            insertOne(Person(
                    name = faker.funnyName().name(),
                    personType = personType,
                    punishment = punishment
            ))
        }

        println("Creating CrimeScene objects...")
        repeat(maxCrimeScenes) {
            insertOne(CrimeScene(
                    name = faker.address().fullAddress(),
                    cityDistrict = Lazy(faker.random().nextInt(maxCityDistricts) + 1)
            ))
        }

        println("Creating Case objects...")
        repeat(maxCases) {
            val caseType = CaseType.values().random()
            insertOne(Case(
                    description = faker.book().title(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees) + 1),
                    caseType = caseType,
                    caseCategory = Lazy(faker.random().nextInt(maxCategories) + 1),
                    crimeScene = if (caseType == CaseType.PROTECTIVE_ACTION) Lazy(faker.random().nextInt(maxCrimeScenes) + 1) else null
            ))
        }

        println("Creating AssignedEmployee objects...")
        repeat(maxAssignedEmployees) {
            insertOne(AssignedEmployee(
                    case = Lazy(faker.random().nextInt(maxCases) + 1 ),
                    employee = Lazy(faker.random().nextInt(maxEmployees) + 1)
            ))
        }

        println("Creating Connection objects...")
        repeat(maxConnections) {
            val personId = faker.random().nextInt(maxPersons) + 1
            insertOne(Connection(
                    case = Lazy(faker.random().nextInt(maxCases) + 1),
                    person = Lazy(personId),
                    crimeScene = Lazy(faker.random().nextInt(maxCrimeScenes) + 1),
                    confirmed = confirmed[personId - 1]
            ))
        }
    }
}