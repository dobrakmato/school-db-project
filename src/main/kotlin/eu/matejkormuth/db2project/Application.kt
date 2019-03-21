package eu.matejkormuth.db2project

import com.github.javafaker.Faker
import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.*

fun main() {
    Application.run()
}

object Application {
    fun run() {

        Database.initialize()

        createTables()
        //fillTables()

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

        val nameItem = FormItem("Name", validations = listOf(NotEmpty))
        val typeItem = FormItem("Type (witness, suspect, victim)", validations = listOf(NotEmpty, OneOf("witness", "suspect", "victim")))
        val form = Form(listOf(nameItem, typeItem), "Create a new Person") {
            transaction {
                val person = insertOne(Person(
                        name = it.getValue(nameItem),
                        personType = PersonType.valueOf(it.getValue(typeItem).toUpperCase())
                ))

                println(retrieve(person))
                println("=======================")
            }
        }

        //Scene.content = form

        val faker = Faker()

        transaction {
            repeat(10) {
                insertOne(Person(
                        name = faker.funnyName().name(),
                        personType = PersonType.values().random()
                ))
            }
        }

        /*
        transaction {
            val case = findOne<Case>(1)

            runQuery("SELECT 1337").use {
                it.next()
                println(it.getInt(1))
            }
        }

        transaction {
            val case = Cases.findById(1)
        }

        transaction {
            val person = findOne<Person>(14)
            val lazy = findOne<Case>(1).caseCategory
            val category = retrieve(lazy)
        }
        */

        transaction {
            val persons = findAll<Person>()
            val table = DataTable(persons, listOf("ID", "Name", "Type")) {
                listOf(it.id.toString(), it.name, it.personType.toString())
            }
            Scene.content = table

            findOne<Person>(1)?.let {
                println("1 = $it")

                updateOne(it.copy(
                        name = "Ivan"
                ))

                println("1 = " + findOne<Person>(1))
            }



            People.findById(4)

            delete<Employee>(3)

            println("ok")
        }


        val log by logger()
    }
}