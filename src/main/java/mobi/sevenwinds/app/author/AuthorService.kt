package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addRecord(body: CreateAuthorRequest): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fio = body.fio
            }

            return@transaction entity.toResponse()
        }
    }
}