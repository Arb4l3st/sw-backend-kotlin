package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorEntity.findById(it) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val allEntities = BudgetEntity.all()
                .filter { it.year == param.year }
                .let { list ->
                    param.authorName?.let { name ->
                        list.filter { entity ->
                            entity.author?.name?.contains(name, ignoreCase = true) ?: false
                        }
                    } ?: list
                }

            val sorted = allEntities.sortedWith(compareBy({ it.month }, { -it.amount }))
            val paginated = sorted.drop(param.offset).take(param.limit)

            val sumByType = allEntities.groupBy { it.type.name }.mapValues { (_, list) -> list.sumOf { it.amount } }

            return@transaction BudgetYearStatsResponse(
                total = allEntities.size,
                totalByType = sumByType,
                items = paginated.map { it.toResponse() }
            )
        }
    }
}