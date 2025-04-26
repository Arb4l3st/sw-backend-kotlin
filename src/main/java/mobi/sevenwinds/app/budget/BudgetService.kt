package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam, authorFioFilter: String?): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {

            val total = BudgetTable
                .select { BudgetTable.year eq param.year }
                .count()

            val query = BudgetTable
                .select { BudgetTable.year eq param.year }
                .apply {
                    if (!authorFioFilter.isNullOrEmpty()) {
                        val filterPattern = "%${authorFioFilter.toLowerCase()}%"
                        this.andWhere {
                            AuthorTable.fio.lowerCase().like(filterPattern)
                        }
                    }
                }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)

            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }


            val allDataForSum = BudgetEntity
                .find { BudgetTable.year eq param.year }
            val sumByType = allDataForSum
                .groupBy { it.type.name }
                .mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}