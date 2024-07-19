package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
//            addLogger(StdOutSqlLogger)

            val sumByTypeQuery = BudgetTable
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select {
                    BudgetTable.year eq param.year
                }
                .groupBy(BudgetTable.type)

            val itemsQuery = BudgetTable
                .select {
                    BudgetTable.year eq param.year
                }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)

            val total = BudgetTable
                .select {
                    BudgetTable.year eq param.year
                }.count()


            val sumByType = sumByTypeQuery.associate {
                it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: 0)
            }
            val data = BudgetEntity.wrapRows(itemsQuery).map { it.toResponse() }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}