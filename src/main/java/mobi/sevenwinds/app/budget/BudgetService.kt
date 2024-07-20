package mobi.sevenwinds.app.budget

import io.ktor.features.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object BudgetService {
    suspend fun addRecord(body: BudgetRequest): BudgetResponse = withContext(Dispatchers.IO) {
        newSuspendedTransaction(Dispatchers.IO) {
            val author = body.authorId?.let { id -> AuthorEntity.findById(id)
                ?: throw NotFoundException("Author with id=$id is not found") }
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = author
            }
            entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        newSuspendedTransaction {
//            addLogger(StdOutSqlLogger)

            return@newSuspendedTransaction BudgetYearStatsResponse(
                total = totalYearStats(param),
                totalByType = totalYearStatsByType(param),
                items = itemsForYearStats(param)
            )
        }
    }

    private fun Transaction.totalYearStatsByType(param: BudgetYearParam) = BudgetTable
        .slice(BudgetTable.type, BudgetTable.amount.sum())
        .select {
            BudgetTable.year eq param.year
        }
        .groupBy(BudgetTable.type)
        .associate {
            it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: 0)
        }

    private fun Transaction.totalYearStats(param: BudgetYearParam) = BudgetTable
        .select {
            BudgetTable.year eq param.year
        }.count()

    private fun Transaction.itemsForYearStats(param: BudgetYearParam) = BudgetTable
        .leftJoin(AuthorTable, {authorId}, {id})
        .select {
            BudgetTable.year eq param.year
        }.run {
            if (param.authorName != null)
                andWhere { LowerCase(AuthorTable.fullName) like "%${param.authorName.toLowerCase()}%" }
            else this
        }
        .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
        .limit(param.limit, param.offset)
        .map { row ->
            if (row[BudgetTable.authorId] != null) AuthorEntity.wrapRow(row)
            BudgetEntity.wrapRow(row).toResponse()
        }
}