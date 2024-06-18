package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorEntity[it]}
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val queryData = (BudgetTable leftJoin AuthorTable)
                .select {(BudgetTable.year eq param.year) and (AuthorTable.name.lowerCase() like "%${param.authorName?.lowercase()}%")}
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)


            val queryCount = BudgetTable
                .select { BudgetTable.year eq param.year }

            val pData = BudgetEntity.wrapRows(queryData)
                .drop(param.offset)
                .take(param.limit)
                .map { it.toStatsResponse() }

            val total = queryCount.count()
            val data = BudgetEntity.wrapRows(queryData).map { it.toResponse() }


            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = pData
            )
        }
    }
}