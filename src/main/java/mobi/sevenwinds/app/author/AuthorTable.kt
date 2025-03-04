package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.UUIDTable
import java.util.UUID

object AuthorTable : UUIDTable("author") {
    val fullName = varchar("full_name", 500)
    val creationDate = datetime("creation_date")
}

class AuthorEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object : UUIDEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var creationDate by AuthorTable.creationDate
}

fun AuthorEntity.toResponse(): AuthorResponse =
    AuthorResponse(id.value, creationDate, fullName)
