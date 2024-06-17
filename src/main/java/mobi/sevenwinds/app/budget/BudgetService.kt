package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetResponseDto = withContext(Dispatchers.IO) {
        transaction {
            val authorEntity = body.authorId?.let { AuthorEntity.findById(it) }
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
            val query = BudgetTable.join(AuthorTable, JoinType.LEFT, additionalConstraint = { BudgetTable.author eq AuthorTable.id })
                .select { BudgetTable.year eq param.year }

            param.authorName?.let {
                query.andWhere { AuthorTable.name.lowerCase() like "%${it.toLowerCase()}%" }
            }

            val total = query.count()

            val data = BudgetEntity.wrapRows(query)
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)
                .map { it.toResponse() }

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