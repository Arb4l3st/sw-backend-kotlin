package mobi.sevenwinds.app.author

import mobi.sevenwinds.app.budget.BudgetEntity
import mobi.sevenwinds.app.budget.BudgetTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object AuthorTable : IntIdTable("author") {
    val fullName = text("full_name")
    val createdAt = datetime("created_at").default(DateTime.now())
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorRs {
        return AuthorRs(fullName, formatCreatedAt(createdAt))
    }
}

fun formatCreatedAt(createdAt: DateTime): String {
    val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
    val formattedCreatedAt = createdAt.toString(formatter)

    return formattedCreatedAt
}

fun findByAuthorId(authorId: Int): AuthorRs? {
    return transaction {
        AuthorTable
            .select { AuthorTable.id eq authorId }
            .mapNotNull { it ->
                AuthorRs(
                    fullName = it[AuthorTable.fullName],
                    createdAt = formatCreatedAt(it[AuthorTable.createdAt])
                )
            }
            .singleOrNull()
    }
}
