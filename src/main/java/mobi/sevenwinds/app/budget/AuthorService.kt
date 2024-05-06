package mobi.sevenwinds.app.budget

import AuthorEntity
import AuthorRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object AuthorService {
    suspend fun addAuthorRecord(body: AuthorRecord, now: LocalDateTime): AuthorRecord =
        withContext(Dispatchers.IO) {
            transaction {
                val entity = AuthorEntity.new {
                    this.fullName = body.fullName
                    this.date = now.toString()
                }

                return@transaction entity.toResponseAuthor()
            }
        }
}