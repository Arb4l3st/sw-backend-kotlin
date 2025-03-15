package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
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
                    this.author = body.authorId?.let { AuthorEntity.findById(body.authorId) }
                }
                return@transaction entity.toResponse()
            }
        }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            var allQueryData = (BudgetTable leftJoin AuthorTable)
                .select { BudgetTable.year eq param.year }
            if (!param.authorName.isNullOrBlank()) {
                allQueryData = allQueryData.andWhere {
                    AuthorTable.fullName.lowerCase() like "%${param.authorName.toLowerCase()}%"
                }
            }
            val total = allQueryData.count()

            val queryOrder = allQueryData
                .orderBy(
                    Pair(BudgetTable.year, SortOrder.ASC),
                    Pair(BudgetTable.month, SortOrder.ASC),
                    Pair(BudgetTable.amount, SortOrder.DESC)
                )

            var data = BudgetEntity.wrapRows(queryOrder).map { it.toResponse() }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            queryOrder.limit(param.limit, param.offset)
            data = BudgetEntity.wrapRows(queryOrder).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data

            )
        }
    }

}