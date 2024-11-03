package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AuthorService {
    suspend fun addAuthor(createAuthorRequestData: CreateAuthorRequestData): AuthorRecord =
        newSuspendedTransaction(Dispatchers.IO) {
            val newAuthor = AuthorEntity.new {
                fullName = createAuthorRequestData.fullName
            }

            newAuthor.refresh(true)

            return@newSuspendedTransaction newAuthor.toResponse()
        }

}