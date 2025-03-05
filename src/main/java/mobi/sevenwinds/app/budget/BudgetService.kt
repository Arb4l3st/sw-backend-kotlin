package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.utils.firstEntityOrNull
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRequest): BudgetResponse = withContext(Dispatchers.IO) {
        transaction {
            val author = AuthorTable.select { AuthorTable.id eq body.authorId }.firstEntityOrNull(AuthorEntity)
            if (body.authorId != null && author == null) {
                throw IllegalArgumentException("Can't find author with the actual id ${body.authorId}")
            }

            BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }.toResponse(author?.fullName, author?.creationDate)
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val (count, sum, type) = arrayOf(BudgetTable.id.count(), BudgetTable.amount.sum(), BudgetTable.type)
            val totalStats = BudgetTable.slice(count, sum, type).selectAll().groupBy(BudgetTable.type).map { row ->
                TemporaryStats(
                    count = row[count] as Int,
                    sum = row[sum] as Int,
                    budgetType = row[type] as BudgetType
                )
            }

            BudgetYearStatsResponse(
                total = totalStats.first { it.budgetType == BudgetType.Приход }.count,
                totalByType = totalStats.groupBy({ it.budgetType.name }, { it.sum }).mapValues { it.value.sum() },
                items = findBudgetStats(param)
            )
        }
    }

    private fun findBudgetStats(param: BudgetYearParam): List<BudgetResponse> =
        (BudgetTable leftJoin AuthorTable).select {
            val mainExpression = BudgetTable.year eq param.year
            val optionalExpression = (AuthorTable.fullName.lowerCase() like "%${param.filter?.lowercase()}%")
            param.filter?.let { mainExpression and optionalExpression } ?: mainExpression
        }.orderBy(
            BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC
        ).limit(param.limit, param.offset).map(ResultRow::toBudgetResponse)

    private data class TemporaryStats(
        val count: Int,
        val sum: Int,
        val budgetType: BudgetType
    )
}