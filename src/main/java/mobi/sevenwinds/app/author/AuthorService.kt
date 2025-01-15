package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object AuthorService {
    suspend fun addRecord(body: AuthorRq): AuthorRs = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fullName = if (body.middleName != null) {
                    val fullName = "${body.lastName} ${body.firstName} ${body.middleName}"
                    fullName.split(" ").joinToString(" ") { it.capitalize() }
                } else {
                    val fullName = "${body.lastName} ${body.firstName}"
                    fullName.split(" ").joinToString(" ") { it.capitalize() }
                }
            }

            return@transaction entity.toResponse()
        }
    }
}
