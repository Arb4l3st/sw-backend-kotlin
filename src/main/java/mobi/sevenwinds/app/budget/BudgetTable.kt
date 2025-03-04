package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.joda.time.DateTime

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val authorId = uuid("author_id").references(AuthorTable.id).nullable()
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var authorId by BudgetTable.authorId

    fun toResponse(
        authorFullName: String? = null,
        creationDate: DateTime? = null
    ): BudgetResponse = BudgetResponse(
        year = year,
        month = month,
        amount = amount,
        type = type,
        authorFullName = authorFullName,
        creationDate = creationDate
    )
}

fun ResultRow.toBudgetResponse() = BudgetResponse(
    year = this[BudgetTable.year],
    month = this[BudgetTable.month],
    amount = this[BudgetTable.amount],
    type = this[BudgetTable.type],
    authorFullName = this[AuthorTable.fullName],
    creationDate = this[AuthorTable.creationDate]
)
