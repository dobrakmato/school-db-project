package eu.matejkormuth.db2project

import com.github.javafaker.Faker
import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.*
import java.util.*
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

fun main() {
    Application.run()
}

object Application {
    fun run() {

        Database.initialize()

        val random = Random()
        val faker = Faker()

        println("Tables created in " + measureTimeMillis {
            createTables()
        } + "ms")
        println("Tables filled in " + measureTimeMillis {
            fillTables()
        } + "ms")

        //Scene.clear()


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
            val persons = findAll<Person>(10)
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


            val department = findOne<Department>(1)
            println(department)
            println(retrieve(department?.headEmployee!!))

            val eagerDepartment = findOne<Department>(1, true)
            println(eagerDepartment)
            println(retrieve(eagerDepartment?.headEmployee!!))


            People.findById(4)

            //delete<Employee>(3)

            println("ok")
        }

        ApplicationUI.mainMenu()


        val log by logger()
    }


}