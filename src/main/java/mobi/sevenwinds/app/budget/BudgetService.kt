package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.stream.Collectors

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId?.let { EntityID(it, AuthorTable) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)


            val queryAll = BudgetTable
                .select { BudgetTable.year eq param.year }

            val total = queryAll.count()

            val paginatedData = BudgetEntity.wrapRows(query).map { it.toResponse() }.filter {
                if(param.filter == null)
                    return@filter true
                if(it.authorId == null)
                    return@filter false
                return@filter it.authorFio!!.toLowerCase().contains(param.filter.toLowerCase())
            }.stream().skip(param.offset.toLong()).limit(param.limit.toLong()).collect(Collectors.toList())

            val allData = BudgetEntity.wrapRows(queryAll).map { it.toResponse() }

            val sumByType = allData.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = paginatedData
            )
        }
    }
}