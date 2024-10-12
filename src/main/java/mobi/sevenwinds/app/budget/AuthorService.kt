package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addRecord(fio: String): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fio = fio
                this.creationDateTime = DateTime.now() // Устанавливаем текущее время, если не указано
                this.type = type
            }

            return@transaction entity.toResponse()
        }
    }
}