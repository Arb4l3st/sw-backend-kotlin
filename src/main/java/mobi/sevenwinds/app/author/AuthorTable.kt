package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val familyName = text("family_name")
    val givenName = text("given_name")
    val patronymic = text("patronymic")
    val dateOfCreate = datetime("date_of_create")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var familyName by AuthorTable.familyName
    var givenName by AuthorTable.givenName
    var patronymic by AuthorTable.patronymic
    var dateOfCreate by AuthorTable.dateOfCreate

    fun toResponse(): AuthorRecord {
        return AuthorRecord(familyName, givenName, patronymic)
    }
}