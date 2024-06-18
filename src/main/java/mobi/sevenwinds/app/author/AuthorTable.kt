package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.CurrentDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object AuthorTable : IntIdTable("author") {
        val name = varchar("full_name",(255))
        val timeStamp = datetime("date_time").defaultExpression(CurrentDateTime())
    }

    class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AuthorEntity>(AuthorTable)

        var name by AuthorTable.name
        val timeStamp by AuthorTable.timeStamp

        fun toResponse(): AuthorRecord {
            return AuthorRecord(id.value,name,timeStamp.toString("yyyy-MM-dd HH:mm:ss"))
        }
    }
