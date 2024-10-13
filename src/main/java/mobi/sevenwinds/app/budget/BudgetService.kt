package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {

            body.author?.let {
                AuthorEntity[it]
            }

            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.author
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .join(AuthorTable, JoinType.LEFT, BudgetTable.author, AuthorTable.id)
                .select { BudgetTable.year eq param.year }
                .limit(param.limit, param.offset)
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)

            var data = query.map { res ->
                ResponseDetailsAuthor(
                    res[BudgetTable.year],
                    res[BudgetTable.month],
                    res[BudgetTable.amount],
                    res[BudgetTable.type],
                    res[BudgetTable.author]?.let {
                        ResponseDetailsAuthor.Author(it, res[AuthorTable.fullName], res[AuthorTable.dateCreated])
                    }
                )
            }

            param.author?.let { authorName ->
                data = data.filter {
                    it.author?.authorFullName?.contains(authorName, ignoreCase = true) == true
                }
            }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }
            val total = data.count()

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}
