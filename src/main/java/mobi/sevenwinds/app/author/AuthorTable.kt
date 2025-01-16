package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val fio = varchar("fio", 150)
    val dateOfCreate = datetime("date_of_create")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var dateOfCreate by AuthorTable.dateOfCreate

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fio)
    }
}