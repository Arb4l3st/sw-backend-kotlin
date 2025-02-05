package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like


object BudgetService {
    suspend fun addRecord(body: BudgetRecordRequest): BudgetRecordResponse = withContext(Dispatchers.IO) {
        transaction {
            val authorEntity = body.author?.let { id ->
                AuthorEntity.findById(EntityID(id, AuthorTable))
            }

            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                if (authorEntity != null) {
                    this.author = authorEntity
                }
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            var query = BudgetTable
                .leftJoin(AuthorTable)
                .select { BudgetTable.year eq param.year }

            param.authorName?.let { name ->
                val lowerName = name.toLowerCase()
                query = query.adjustWhere { AuthorTable.name.lowerCase() like "%$lowerName%" }
            }

            val total = query.count()

            param.sortBy?.let { sortField ->
                val sortOrder = SortOrder.valueOf(param.order ?: "ASC")
                query = when (sortField) {
                    "month" -> {
                        if (sortOrder == SortOrder.ASC) {
                            query.orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
                        } else {
                            query.orderBy(BudgetTable.month to SortOrder.DESC, BudgetTable.amount to SortOrder.ASC)
                        }
                    }
                    "amount" -> {
                        if (sortOrder == SortOrder.ASC) {
                            query.orderBy(BudgetTable.amount to SortOrder.ASC, BudgetTable.month to SortOrder.ASC)
                        } else {
                            query.orderBy(BudgetTable.amount to SortOrder.DESC, BudgetTable.month to SortOrder.ASC)
                        }
                    }
                    else -> query
                }
            }


            query = query.limit(param.limit, param.offset)

            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = data.groupBy { it.type.name }
                .mapValues { (_, items) -> items.sumOf { it.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}
