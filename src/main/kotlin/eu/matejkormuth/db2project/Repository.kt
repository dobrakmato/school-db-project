package eu.matejkormuth.db2project

import eu.matejkormuth.db2project.models.Case
import eu.matejkormuth.db2project.models.Category
import eu.matejkormuth.db2project.models.Person

interface Repository<T : Entity> {
    fun findById(id: Id): T
    fun findAll(): Iterable<T>
    fun insert(entity: T)
    fun deleteById(id: Id)
    fun update(entity: T)
}

inline fun <reified T : Entity> repository(): Repository<T> {
    return DefaultRepositoryImplementation(T::class.java)
}

class DefaultRepositoryImplementation<T : Entity>(
        private val klass: Class<T>,
        private val table: Table<T> = Database.tableFor(klass)
) : Repository<T> {
    override fun update(entity: T) {
        table.queryBuilder()
                .updateOne(entity)
    }

    override fun deleteById(id: Id) {
        table.queryBuilder()
                .delete()
                .eq("id", id)
                .execute()
    }

    override fun insert(entity: T) {
        table.queryBuilder()
                .insertOne(entity)
    }

    override fun findById(id: Id): T {
        return table.queryBuilder()
                .select()
                .eq("id", id)
                .fetchOne()
    }

    override fun findAll(): Iterable<T> {
        return table.queryBuilder()
                .select()
                .fetchMultiple()
    }

}

object People : Repository<Person> by repository()
object Cases : Repository<Case> by repository()
object Categories : Repository<Category> by repository()