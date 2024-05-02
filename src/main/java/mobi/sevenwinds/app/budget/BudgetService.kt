package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorRecord
import mobi.sevenwinds.app.author.AuthorTable
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
                this.authorId = body.authorId
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam,authorNameFilter: String? = null): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .join(AuthorTable, JoinType.INNER, BudgetTable.authorId, AuthorTable.id)
                .select { BudgetTable.year eq param.year }
                .limit(param.limit, param.offset)

            if (!authorNameFilter.isNullOrBlank()) {
                query.andWhere { AuthorTable.fullName.lowerCase() like "%${authorNameFilter.toLowerCase()}%" }
            }

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }
            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            val authorData = if (data.isNotEmpty()) {
                val authorEntity = AuthorEntity.findById(data.first().authorId ?: -1)
                authorEntity?.let { AuthorRecord(it.fullName, it.dateCreate) }
            } else null

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data,
                authorFullName = authorData?.fullName,
                authorDateCreate = authorData?.dateCreate
            )
        }
    }
}