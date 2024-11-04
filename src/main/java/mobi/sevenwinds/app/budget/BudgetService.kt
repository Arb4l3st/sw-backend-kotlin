package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object BudgetService {
    suspend fun addRecord(body: AddBudgetRecordData): BudgetRecordData =
        newSuspendedTransaction(Dispatchers.IO) {
            BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorEntity = body.authorId?.let { AuthorEntity[it] }
            }.toResponse()
        }

    suspend fun getYearStats(param: BudgetYearParams): BudgetYearStatsData =
        newSuspendedTransaction(Dispatchers.IO) {
            val (totalOperationsCountForYear, totalAmountByTypes) = getYearStats(param.year)
            val yearBudgetRecordsPage = getYearBudgetRecordsPage(param)

            return@newSuspendedTransaction BudgetYearStatsData(
                total = totalOperationsCountForYear,
                totalByType = totalAmountByTypes,
                items = yearBudgetRecordsPage
            )
        }


    /**
     * @param year Int value of year for fetching statistics.
     * @return Pair<Int, Map<String, Int>> object containing:
     *   - first: Total operations count for specified year.
     *   - second: A map of total year amounts categorized by type.
     */
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

    private fun getYearBudgetRecordsPage(params: BudgetYearParams): List<BudgetRecordData> {
        val (year, limit, offset, authorName) = params

        return BudgetTable
            .leftJoin(AuthorTable, { authorId }, { id })
            .slice(
                BudgetTable.id, BudgetTable.type, BudgetTable.year,
                BudgetTable.month, BudgetTable.amount, BudgetTable.authorId,
                AuthorTable.fullName, AuthorTable.creationDateTime
            ).select { getWhereCondition(year, authorName) }
            .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
            .limit(limit, offset)
            .map(BudgetEntity.Companion::wrapRow)
            .map(BudgetEntity::toResponse)
    }

    private fun getWhereCondition(year: Int, authorName: String?): Op<Boolean> {
        val yearCondition = BudgetTable.year eq year

        return authorName
            ?.let { yearCondition and (AuthorTable.fullName iLike "%$authorName%") }
            ?: yearCondition
    }

    private infix fun Column<String>.iLike(term: String) = Op.build {
        this@iLike.lowerCase() like term.toLowerCase()
    }
}