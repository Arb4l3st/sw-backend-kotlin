package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorResponse = withContext(Dispatchers.IO) {
        return@withContext transaction {
            return@transaction AuthorEntity.new {
                this.fio = body.fio
            }
        }.toResponse()
    }
}