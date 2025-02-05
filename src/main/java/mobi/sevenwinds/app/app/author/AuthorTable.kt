package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime


object AuthorTable : IntIdTable("author") {
    val name = varchar("name", 255)
    val createdAt = datetime("created_at").default(DateTime.now())
}

