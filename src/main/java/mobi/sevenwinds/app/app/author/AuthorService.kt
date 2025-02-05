package mobi.sevenwinds.app.author


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object AuthorService {
    fun addAuthor(body: CreateAuthorRequest): AuthorResponse {
        return transaction {
            val author = AuthorEntity.new {
                this.name = body.name
                this.createdAt = DateTime.now()
            }
            author.toResponse()
        }
    }

    suspend fun getAllAuthors(): List<AuthorResponse> = withContext(Dispatchers.IO) {
        transaction {
            AuthorEntity.all().map { it.toResponse() }
        }
    }

}