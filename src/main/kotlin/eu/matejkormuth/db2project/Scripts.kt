package eu.matejkormuth.db2project

import com.github.javafaker.Faker
import eu.matejkormuth.db2project.models.*

fun createTables() {
    Database.getConnection().use { conn ->
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
        ).forEach { sql ->
            conn.createStatement().use {
                QueryBuilder.log.debug(sql)
                it.execute(sql)
            }
        }
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

        println("Creating Employee objects...")
        repeat(maxEmployees) {
            val type = EmployeeType.values().random()
            insertOne(Employee(
                    name = faker.name().fullName(),
                    type = type,
                    department = Lazy(faker.random().nextInt(maxDepartments)),
                    rank = if (type == EmployeeType.POLICEMAN) faker.random().nextInt(20) else null
            ))
        }

        println("Creating Person objects...")
        repeat(maxPersons) {
            var punishment: Lazy<Punishment>? = null
            val personType = PersonType.values().random()

            if (personType == PersonType.SUSPECT && confirmed[it]) {
                val punishmentType = PunishmentType.values().random()
                punishment = insertOne(Punishment(
                        punishmentType = punishmentType,
                        fineAmount = if (punishmentType == PunishmentType.FINE) faker.random().nextInt(2000) else null
                ))
            }

            insertOne(Person(
                    name = faker.funnyName().name(),
                    personType = personType,
                    punishment = punishment
            ))
        }

        println("Creating Department objects...")
        repeat(maxDepartments) {
            insertOne(Department(
                    name = faker.commerce().department(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees))
            ))
        }

        println("Creating CrimeScene objects...")
        repeat(maxCrimeScenes) {
            insertOne(CrimeScene(
                    name = faker.address().fullAddress(),
                    cityDistrict = Lazy(faker.random().nextInt(maxCityDistricts))
            ))
        }

        println("Creating Case objects...")
        repeat(maxCases) {
            val caseType = CaseType.values().random()
            insertOne(Case(
                    description = faker.book().title(),
                    headEmployee = Lazy(faker.random().nextInt(maxEmployees)),
                    caseType = caseType,
                    caseCategory = Lazy(faker.random().nextInt(maxCategories)),
                    crimeScene = if (caseType == CaseType.PROTECTIVE_ACTION) Lazy(faker.random().nextInt(maxCrimeScenes)) else null
            ))
        }

        println("Creating AssignedEmployee objects...")
        repeat(maxAssignedEmployees) {
            insertOne(AssignedEmployee(
                    case = Lazy(faker.random().nextInt(maxCases)),
                    employee = Lazy(faker.random().nextInt(maxEmployees))
            ))
        }

        println("Creating Connection objects...")
        repeat(maxConnections) {
            val personId = faker.random().nextInt(maxPersons)
            insertOne(Connection(
                    case = Lazy(faker.random().nextInt(maxCases)),
                    person = Lazy(personId),
                    crimeScene = Lazy(faker.random().nextInt(maxCrimeScenes)),
                    confirmed = confirmed[personId]
            ))
        }
    }
}