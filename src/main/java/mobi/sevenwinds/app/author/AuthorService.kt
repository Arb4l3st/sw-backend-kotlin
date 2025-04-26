package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addAuthor(fio: String): AuthorApi.AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val authorEntity = AuthorEntity.new {
                this.fio = fio
                this.createdDate = DateTime.now()
            }

            return@transaction authorEntity.toResponse()
        }
    }
}