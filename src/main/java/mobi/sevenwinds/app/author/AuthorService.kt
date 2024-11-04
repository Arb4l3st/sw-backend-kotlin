package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.familyName = body.familyName
                this.givenName = body.givenName
                this.patronymic = body.patronymic
                this.dateOfCreate = body.dateOfCreate
            }

            return@transaction entity.toResponse()
        }
    }

}