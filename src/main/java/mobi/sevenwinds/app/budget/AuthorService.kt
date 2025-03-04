package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    suspend fun addAuthor(request: AuthorRequest): AuthorResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                fullName = request.fullName
                createdAt = DateTime.now()
            }
            return@transaction entity.toResponse()
        }
    }
}

data class AuthorRequest(val fullName: String)
data class AuthorResponse(val id: Int, val fullName: String, val createdAt: DateTime)