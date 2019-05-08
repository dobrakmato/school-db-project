package eu.matejkormuth.db2project.screens

import eu.matejkormuth.db2project.*
import eu.matejkormuth.db2project.models.CityDistrict
import eu.matejkormuth.db2project.models.CrimeScene
import eu.matejkormuth.db2project.ui.*
import java.sql.SQLException

/**
 * Methods related to UI related to crime scenes.
 */
object CrimeSceneUI {

    /**
     * Creates and returns UI element used for listing crime scenes.
     */
    fun listCrimeScenes(): Drawable {
        val departments = transaction { findAll<CrimeScene>(eagerLoad = true) }

        return DataTable(departments, listOf("ID", "Name", "City district")) {
            listOf(it.id.toString(), it.name, it.cityDistrict.getOrNull()!!.name)
        }
    }

    /**
     * Creates and returns UI element used for creating crimes scene.
     */
    fun createCrimeScene(): Drawable {
        val name = FormItem.required("Name of crime scene")
        val cityDistrictId = FormItem.requiredId("Associated city district ID")

        return Form(listOf(name, cityDistrictId), "[ Form - Create crime scene ]") {
            try {
                transaction {
                    val cityDistrict = findOne<CityDistrict>(it[cityDistrictId].toInt())
                            ?: throw RuntimeException("Specified city district does not exists!")
                    val crimeScene = insertOne(CrimeScene(
                            name = it[name],
                            cityDistrict = Lazy(cityDistrict.id)
                    ))
                    Scene.replace(Success("Crime scene created (${crimeScene.id})!"))
                }
            } catch (ex: Exception) {
                if (ex is SQLException) {
                    Scene.replace(Error("Insert operation failed! Check your information."))
                } else {
                    Scene.replace(Error("Insert operation failed! ${ex.message}"))
                }
            }
        }
    }

    /**
     * Creates and returns UI element used for updating crimes scene.
     */
    fun updateCrimeScene(): Drawable {
        val crimeSceneId = FormItem.requiredId("Crime scene ID")
        val name = FormItem.required("Name of crime scene")

        return Form(listOf(crimeSceneId, name), "[ Form - Update crime scene ]") {
            try {
                transaction {
                    val crimeScene = findOne<CrimeScene>(it[crimeSceneId].toInt())
                            ?: throw RuntimeException("Crime scene not found!")

                    updateOne(crimeScene.copy(
                            name = it[name]
                    ))

                    Scene.replace(Success("Crime scene updated!"))
                }
            } catch (ex: Exception) {
                if (ex is SQLException) {
                    Scene.replace(Error("Update operation failed! Check your information."))
                } else {
                    Scene.replace(Error("Update operation failed! ${ex.message}"))
                }
            }
        }
    }

}
