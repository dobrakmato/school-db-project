package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.findAll
import eu.matejkormuth.db2project.getOrNull
import eu.matejkormuth.db2project.models.*
import eu.matejkormuth.db2project.transaction
import eu.matejkormuth.db2project.ui.*
import java.sql.SQLException

/**
 * Methods related to UI related to punishments.
 */
object PunishmentUI {

    /**
     * Creates and returns UI element used for listing punishments.
     */
    fun listPunishments(): Drawable {
        val punishments = transaction { findAll<Punishment>(eagerLoad = true) }

        return DataTable(punishments, listOf("ID", "Person", "Type", "Fine amount")) {
            listOf(it.id.toString(), it.punished.getOrNull()!!.name,
                    it.punishmentType.toString(), it.fineAmount?.toString() ?: "")
        }
    }

    /**
     * Creates and returns UI element used for creating punishments.
     */
    fun createPunishment(): Drawable {
        val personId = FormItem.requiredId("Person ID")

        return Form(listOf(personId), "[Form - Punish a person]") {
            try {
                Person.punish(it[personId].toInt())
                Scene.replace(Success("Person punished."))
            } catch (ex: Exception) {
                if (ex is SQLException) {
                    Scene.replace(Error("Cannot punish specified person. Detail: Already punished"))
                } else {
                    Scene.replace(Error("Cannot punish specified person. Detail: ${ex.message}"))
                }
            }
        }
    }
}