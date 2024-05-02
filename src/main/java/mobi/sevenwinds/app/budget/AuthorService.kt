package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.lastName = body.lastName
                this.firstName = body.firstName
                this.fatherName = body.fatherName
                this.dateOfCreate = body.dateOfCreate!!
            }

            return@transaction entity.toResponse()
        }
    }
}
