package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime

object AuthorTable : IntIdTable("author") {
    val name = text("name")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime())
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    private var createdAt by AuthorTable.createdAt

    var name by AuthorTable.name

    fun toResponse(): AuthorResponse {
        return AuthorResponse(id.value, name, createdAt.toString("dd-MM-yyyy HH:mm:ss"))
    }
}