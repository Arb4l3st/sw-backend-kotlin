package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.leftJoin
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
                this.author = body.authorId?.let { AuthorEntity.findById(it) }
            }

            return@transaction entity.toRecord()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .leftJoin(AuthorTable, { author }, { id })
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month)
                .orderBy(BudgetTable.amount to SortOrder.DESC)

            param.authorNameFilter?.let {
                val filter = it.toLowerCase()
                query.andWhere { AuthorTable.fullName.lowerCase() like "%$filter%" }
            }

            val total = query.count()
            query.limit(param.limit, param.offset * param.limit)

            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }
            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}