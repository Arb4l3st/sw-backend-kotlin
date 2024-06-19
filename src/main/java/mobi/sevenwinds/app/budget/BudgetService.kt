package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord<AuthorIdRequest>): BudgetRecord<AuthorResponse> = withContext(Dispatchers.IO) {
        transaction {
            val authorEntity = body.author?.let { AuthorEntity.findById(it.id) }
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = authorEntity
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = param.authorName?.let {
                BudgetTable
                    .join(AuthorTable, JoinType.LEFT)
                    .select { BudgetTable.year eq param.year }
                    .andWhere { AuthorTable.name.lowerCase() like "%${it.toLowerCase()}%" }
            } ?: let {
                BudgetTable
                    .select { BudgetTable.year eq param.year }
            }

            val total = query.count()

            val data = BudgetEntity.wrapRows(
                query
                    .limit(param.limit, param.offset)
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
            ).map { it.toResponse() }

            val sumByType = BudgetTable
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)
                .associate { it[BudgetTable.type].name to it[BudgetTable.amount.sum()]!! }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}