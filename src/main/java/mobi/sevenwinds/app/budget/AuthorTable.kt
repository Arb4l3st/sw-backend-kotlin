package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("budget") {
    val fio = text("fio")
    val creationDateTime = date("timestamp")
    val type = enumerationByName("type", 100, AuthorType::class)
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var creationDateTime by AuthorTable.creationDateTime
    var type by AuthorTable.type

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fio, creationDateTime, type)
    }
}