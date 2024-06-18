package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRequest): BudgetResponse = withContext(Dispatchers.IO) {
        transaction {
            val author = body.authorId?.let { AuthorEntity.findById(it) }

            if (body.authorId != null && author == null) {
                throw IllegalArgumentException("Invalid author id: ${body.authorId}")
            }

            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = author
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = (BudgetTable leftJoin AuthorTable)
                .select { BudgetTable.year eq param.year }

            val aggregatingQuery = (BudgetTable leftJoin AuthorTable)
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)

            val sortedAndPaginatedQuery = (BudgetTable leftJoin AuthorTable)
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)

            param.authorFullName?.let {
                val filter: SqlExpressionBuilder.() -> Op<Boolean> = {
                    AuthorTable.fullName like "${param.authorFullName}"
                }

                query.andWhere(filter)
                aggregatingQuery.andWhere(filter)
                sortedAndPaginatedQuery.andWhere(filter)
            }

            val total = query.count()

            val totalByType = aggregatingQuery
                .associate { it[BudgetTable.type].name to it[BudgetTable.amount.sum()]!! }

            val items = BudgetEntity
                .wrapRows(sortedAndPaginatedQuery)
                .map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = totalByType,
                items = items
            )
        }
    }
}