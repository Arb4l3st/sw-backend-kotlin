package mobi.sevenwinds.app.author

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AuthorService {
    suspend fun addAuthor(addAuthorRecordData: AddAuthorRecordData): AuthorRecord = newSuspendedTransaction {
        val newAuthor = AuthorEntity.new {
            fullName = addAuthorRecordData.fullName
        }

        newAuthor.refresh(true)

        return@newSuspendedTransaction newAuthor.toResponse()
    }
}