package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.QueryBuilder.Companion.log
import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.Menu
import eu.matejkormuth.db2project.ui.MenuItem

fun main() {
    Application.run()
}

object Application {
    fun run() {
        Database.initialize()
        //Scene.clear()

        val mainMenu = Menu(listOf(
                MenuItem("Employees"),
                MenuItem("Departments"),
                MenuItem("Cases"),
                MenuItem("Crime scenes"),
                MenuItem("Punishments"),
                MenuItem("☠️ Dangerous city districts"),
                MenuItem("\uD83D\uDCC8 Cop of month")
        ), header = "[ ⭐⭐ POLICE DEPARTMENT - MENU ⭐⭐ ]")

        val entities = arrayOf(Case::class.java, Category::class.java, CityDistrict::class.java,
                Connection::class.java, CrimeScene::class.java, Department::class.java,
                Employee::class.java, Person::class.java, AssignedPerson::class.java, Punishment::class.java)

        Database.getConnection().use { conn ->
            DDL.createScript(*entities).forEach { sql ->
                conn.createStatement().use {
                    log.debug(sql)
                    it.execute(sql)
                }
            }


            //Scene.content = mainMenu

            insert(Person(
                    name = "matej",
                    personType = PersonType.WITNESS
            ))

            People.insert(Person(
                    name = "juraj",
                    personType = PersonType.SUSPECT
            ))

            findAll<Person>().forEach {
                println(it)
            }

            val administrator = findOne<Person>(1)
            // val updated = administrator.copy(
            //         name = "Ivan"
            // ).save()

            println(administrator)
            delete<Employee>(3)

            println("ok")
        }

        val log by logger()
    }
}