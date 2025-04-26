package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("author") {
    val fio = varchar("fio", 255)
    val createdDate = datetime("created_date")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var createdDate by AuthorTable.createdDate

    fun toResponse(): AuthorApi.AuthorRecord {
        return AuthorApi.AuthorRecord(id.value, fio, createdDate)
    }
}

