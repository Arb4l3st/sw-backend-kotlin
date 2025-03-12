package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SortOrder
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

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse =
        newSuspendedTransaction {
            val yearBudgetRecordsPage = getYearBudgetRecordsPage(param)
            val (totalOperationsCountForYear, totalAmountByTypes) = getYearStats(param.year)

            return@newSuspendedTransaction BudgetYearStatsResponse(
                total = totalOperationsCountForYear,
                totalByType = totalAmountByTypes,
                items = yearBudgetRecordsPage
            )
        }

    private fun getYearStats(year: Int): Pair<Int, Map<String, Int>> {
        val typeColumn = BudgetTable.type
        val yearColumn = BudgetTable.year
        val operationsCountForType = BudgetTable.id.count()
        val operationsAmountSumForType = BudgetTable.amount.sum()

        // Use aggregations in one query to get stats -
        // all operations count and operations sum by every type for some year
        val aggregationResult = BudgetTable
            .slice(typeColumn, operationsCountForType, operationsAmountSumForType)
            .select { yearColumn eq year }
            .groupBy(typeColumn)
            .toList()

        // Sum all operations count
        val totalOperationsCountForYear = aggregationResult
            .asSequence()
            .map { row -> row[operationsCountForType] }
            .sum()

        val totalAmountByTypes = aggregationResult.associate { row ->
            val type = row[typeColumn]
            val totalAmount = row[operationsAmountSumForType] ?: 0
            type.name to totalAmount
        }

        return totalOperationsCountForYear to totalAmountByTypes
    }

    private fun getYearBudgetRecordsPage(param: BudgetYearParam): List<BudgetRecord> {
        val defaultSorts = arrayOf(
            BudgetTable.month to SortOrder.ASC,
            BudgetTable.amount to SortOrder.DESC)

        val operationsByYearPage =
            BudgetTable
                .select { BudgetTable.year eq param.year }
                .limit(param.limit, param.offset)

        return BudgetEntity
            .wrapRows(operationsByYearPage)
            .map(BudgetEntity::toResponse)
    }
}