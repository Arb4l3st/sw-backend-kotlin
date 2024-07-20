package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.joda.time.Instant

object AuthorService {
    suspend fun addRecord(body: AuthorRequest): AuthorResponse = withContext(Dispatchers.IO) {
        newSuspendedTransaction {
            AuthorEntity.new {
                fullName = body.fullName
                createdAt = Instant.now().toDateTime()
            }.toResponse()
        }
    }
}