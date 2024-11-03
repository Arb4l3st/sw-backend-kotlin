package mobi.sevenwinds.app.author

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addAuthor(addAuthorRecordData: AddAuthorRecordData): AuthorRecord =
        newSuspendedTransaction {
            AuthorEntity.new {
                fullName = addAuthorRecordData.fullName
                creationDateTime = DateTime()
            }.toResponse()
        }
}