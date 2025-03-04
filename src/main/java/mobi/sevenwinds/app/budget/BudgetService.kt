package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.utils.firstEntityOrNull
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRequest): BudgetResponse = withContext(Dispatchers.IO) {
        transaction {
            val author = AuthorTable
                .select { AuthorTable.id eq body.authorId }
                .firstEntityOrNull(AuthorEntity)

            if (body.authorId != null && author == null) {
                throw IllegalArgumentException("Can't find author with actual id ${body.authorId}")
            }

            return@transaction BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }.toResponse(author?.fullName, author?.creationDate)
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .join(
                    joinType = JoinType.LEFT,
                    onColumn = BudgetTable.authorId,
                    otherTable = AuthorTable,
                    otherColumn = AuthorTable.id
                ).select { BudgetTable.year eq param.year }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)

            val total = query.count()
            val data = query.map(ResultRow::toBudgetResponse)

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data.asSequence().drop(param.offset).take(param.limit).toList()
            )
        }
    }
}