package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val authorId = integer("authorId").references(AuthorTable.id)   // Внешний ключ на таблицу авторов
    val type = enumerationByName("type", 100, BudgetType::class)
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var authorId by BudgetTable.authorId
    val author by AuthorEntity referencedOn BudgetTable.authorId  // Ссылка на объект автора
    var type by BudgetTable.type

    fun toResponse(): BudgetRecord {
        return BudgetRecord(
            year,
            month,
            amount,
            author.toResponse().id.value,
            type
        )
    }
}