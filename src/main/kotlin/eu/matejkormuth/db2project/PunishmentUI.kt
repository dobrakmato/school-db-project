package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.ui.*

object PunishmentUI {

    fun listPunishments(): Drawable {
        val punishments = transaction { findAll<Punishment>(eagerLoad = true) }

        return DataTable(punishments, listOf("ID", "Person", "Type", "Fine amount")) {
            listOf(it.id.toString(), it.punished.getOrNull()!!.name,
                    it.punishmentType.toString(), it.fineAmount?.toString() ?: "")
        }
    }

    fun createPunishment(): Drawable {
        val personId = FormItem.requiredId("Person ID")

        return Form(listOf(personId), "[Form - Punish a person]") {
            transaction {
                try {
                    val person = findOne<Person>(it[personId].toInt()) ?: throw RuntimeException("Person not found!")

                    person.punish(this)
                    Scene.replace(Success("Person punished."))
                } catch (ex: Exception) {
                    rollback()
                    Scene.replace(Error("Cannot punish specified person. Detail: ${ex.message}"))
                }
            }
        }
    }
}