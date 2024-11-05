package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addAuthor(addAuthorRecordData: AddAuthorRecordData): AuthorRecord =
        newSuspendedTransaction(Dispatchers.IO) {
            AuthorEntity.new {
                fullName = addAuthorRecordData.fullName
                creationDateTime = DateTime()
            }.toResponse()
        }
}