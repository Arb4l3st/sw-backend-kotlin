package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime

object AuthorTable : IntIdTable("author") {
    val name = text("name")
    val creationDate = datetime("creation_date").default(DateTime.now())
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<AuthorEntity> (AuthorTable)

    var name by AuthorTable.name
    var creationDate by AuthorTable.creationDate

    fun toResponse(): AuthorResponse {
        return AuthorResponse(id.value, name, creationDate)
    }

}