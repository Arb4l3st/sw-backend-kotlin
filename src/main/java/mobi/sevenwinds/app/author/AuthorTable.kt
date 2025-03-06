package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorTable : IntIdTable("author") {
    val fullName = varchar("full_name", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime())

    fun createAuthor(request: CreateAuthorRequest): AuthorResponse {
        return transaction {
            val author = AuthorEntity.new {
                fullName = request.fullName
            }
            author.toResponse()
        }
    }
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorResponse {
        return AuthorResponse(
            id = id.value,
            fullName = fullName,
            createdAt = createdAt
        )
    }
}