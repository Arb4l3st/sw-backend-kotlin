package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addRecord(body: AuthorRequest): AuthorResponse = withContext(Dispatchers.IO) {
        val entity = transaction {
            AuthorEntity.new {
                this.fullName = body.fullName
            }
        }

        return@withContext entity.toResponse()
    }
}