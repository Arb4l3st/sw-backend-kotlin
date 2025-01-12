package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addAuthor(body: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fullName = body.fullName
                this.creationDate = body.creationDate
            }

            return@transaction entity.toResponse()
        }
    }
}
