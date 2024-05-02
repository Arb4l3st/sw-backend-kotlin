package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val lastName = text("last_name")
    val firstName = text("first_name")
    val fatherName = text("father_name")
    val dateOfCreate = text("date_of_create")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var lastName by AuthorTable.lastName
    var firstName by AuthorTable.firstName
    var fatherName by AuthorTable.fatherName
    var dateOfCreate by AuthorTable.dateOfCreate

    fun toResponse(): AuthorRecord {
        return AuthorRecord(lastName, firstName, fatherName, dateOfCreate)
    }
}