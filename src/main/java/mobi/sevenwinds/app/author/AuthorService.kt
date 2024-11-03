package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AuthorService {
    suspend fun addAuthor(body: CreateAuthorRequestData): AuthorRecord =
        newSuspendedTransaction(Dispatchers.IO) {
            return@newSuspendedTransaction AuthorEntity
                .new { fullName = body.fullName }
                .toResponse()
        }
}