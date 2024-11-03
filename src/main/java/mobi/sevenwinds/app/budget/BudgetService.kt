package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: AddBudgetRecordData): BudgetRecordData = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorEntity = body.authorId?.let { AuthorEntity[it] }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParams): BudgetYearStatsData {
        return newSuspendedTransaction {
            val yearBudgetRecordsPage = getYearBudgetRecordsPage(param)
            val (totalOperationsCountForYear, totalAmountByTypes) = getYearStats(param.year)

            return@newSuspendedTransaction BudgetYearStatsData(
                total = totalOperationsCountForYear,
                totalByType = totalAmountByTypes,
                items = yearBudgetRecordsPage
            )
        }
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

    private fun getYearBudgetRecordsPage(param: BudgetYearParams): List<BudgetRecordData> {
        val yearCondition = BudgetTable.year.eq(param.year)

        val fullCondition = param.authorName?.let { authorName ->
            yearCondition.and(AuthorTable.fullName like "%$authorName%")
        } ?: yearCondition


        return BudgetTable
            .leftJoin(AuthorTable, { authorId }, { id })
            .slice(
                BudgetTable.id, BudgetTable.type, BudgetTable.year,
                BudgetTable.month, BudgetTable.amount, BudgetTable.authorId,

                AuthorTable.fullName, AuthorTable.creationDateTime
            ).select { fullCondition }
            .orderBy(
                BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC
            ).limit(param.limit, param.offset)
            .map(BudgetEntity.Companion::wrapRow)
            .map(BudgetEntity::toResponse)
    }
}