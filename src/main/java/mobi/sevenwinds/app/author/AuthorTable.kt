package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime

object AuthorTable : IntIdTable("author") {
    val fullName = text("full_name")
    val dateCreated = datetime("date_created").default(DateTime.now())
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    val dateCreated by AuthorTable.dateCreated

    fun toResponse(): AuthorResponse {
        return AuthorResponse(fullName, dateCreated)
    }
}