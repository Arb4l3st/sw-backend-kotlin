package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRequest): BudgetResponse = withContext(Dispatchers.IO) {
//        newSuspendedTransaction(Dispatchers.IO) {
        transaction {
//            val author = body.authorId?.let { AuthorService.getRecord(it) }
//            val author = body.authorId?.let { AuthorService.getRecordSuspend(it) }
            val author = body.authorId?.let { id -> AuthorEntity.findById(id) }
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
        transaction {
            addLogger(StdOutSqlLogger)

            val sumByTypeQuery = BudgetTable
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select {
                    BudgetTable.year eq param.year
                }
                .groupBy(BudgetTable.type)

            val itemsQuery = BudgetTable
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

            val total = BudgetTable
                .select {
                    BudgetTable.year eq param.year
                }.count()


            val sumByType = sumByTypeQuery.associate {
                it[BudgetTable.type].name to (it[BudgetTable.amount.sum()] ?: 0)
            }
            val data = BudgetEntity.wrapRows(itemsQuery).map { it.toResponse() }
//            data.forEach { println(it.author?.fullName) }
//                        data.forEach { println(it) }


            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}