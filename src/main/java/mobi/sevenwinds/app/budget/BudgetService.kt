package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
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
                this.authorId = body.authorId?.let { EntityID(it, AuthorTable) }
            }

            val author = entity.authorId?.let {
                AuthorEntity.findById(it)
            }

            return@transaction entity.toResponse(author)
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query =
                if (param.fio != null) {
                    BudgetTable.leftJoin(AuthorTable)
                        .select {
                            (BudgetTable.year eq param.year) and
                                    (AuthorTable.fio.lowerCase().like("%${param.fio.toLowerCase()}%"))
                        }
                } else {
                    BudgetTable
                        .select { BudgetTable.year eq param.year }
                }

            query.orderBy(
                    Pair(BudgetTable.year, SortOrder.ASC),
                    Pair(BudgetTable.month, SortOrder.ASC),
                    Pair(BudgetTable.amount, SortOrder.DESC)
                )

            val total = query.count()

            var data = BudgetEntity.wrapRows(query).map { it.toResponse(null) }
            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            query.limit(param.limit, param.offset)
            data = BudgetEntity.wrapRows(query).map { r ->
                val author = r.authorId?.let {
                    AuthorEntity.findById(it)
                }
                r.toResponse(author)
            }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}