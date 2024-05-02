package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val author_id = optReference("author_id", AuthorTable.id)
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var authorId by BudgetTable.author_id

    fun toResponse(): BudgetRecord {
        val authorFio = authorId?.let {
            transaction {
                val query = AuthorTable
                    .select { AuthorTable.id eq authorId?.value }
                val entity = AuthorEntity.wrapRow(query.first())
                return@transaction "${entity.lastName} ${entity.firstName} ${entity.fatherName}"
            }
        }
        val authorDateOfCreate = authorId?.let {
            transaction {
                val query = AuthorTable
                    .select { AuthorTable.id eq authorId?.value }
                return@transaction AuthorEntity.wrapRow(query.first()).dateOfCreate
            }
        }
        return BudgetRecord(year, month, amount, type, authorId?.value, authorFio, authorDateOfCreate)
    }
}