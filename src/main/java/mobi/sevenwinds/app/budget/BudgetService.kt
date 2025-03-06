package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.author.AuthorTable.fullName
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetTable.BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                body.authorId?.let { authorId ->
                    val author = AuthorEntity.findById(authorId)
                        ?: throw IllegalArgumentException("Автор с ID $authorId не найден")
                    this.author = author
                }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val authorAlias = AuthorTable.alias("author")

            val baseCondition = BudgetTable.year eq param.year

            val finalCondition = if (!param.authorNameFilter.isNullOrEmpty()) {
                baseCondition and (authorAlias[fullName] eq param.authorNameFilter)
            } else {
                baseCondition
            }

            val total = BudgetTable
                .leftJoin(authorAlias, { author }, { authorAlias[AuthorTable.id] })
                .select { finalCondition }
                .count()

            val query = BudgetTable
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                .limit(param.limit, param.offset)

            val data = BudgetTable.BudgetEntity.wrapRows(query).map { it.toResponse() }.let { data ->
                if (!param.authorNameFilter.isNullOrEmpty()) {
                    data.filter { it.authorFullName.equals(param.authorNameFilter, ignoreCase = true) }
                } else {
                    data
                }
            }

            val sumByTypeTotal = BudgetTable
                .leftJoin(authorAlias, { author }, { authorAlias[AuthorTable.id] })
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select { finalCondition }
                .groupBy(BudgetTable.type)
                .associate { it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: 0) }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByTypeTotal,
                items = data
            )
        }
    }
}