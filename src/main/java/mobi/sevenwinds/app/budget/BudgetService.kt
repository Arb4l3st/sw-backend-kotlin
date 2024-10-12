package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.andWhere
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
                this.authorId = body.authorId
                this.type = body.type
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            // Строим запрос с фильтром по году
            var query = (BudgetTable innerJoin AuthorTable)
                .select { BudgetTable.year eq param.year }

            // Добавляем фильтрацию по ФИО автора, если параметр authorName указан
            param.authorName?.let { name ->
                query = query.andWhere { AuthorTable.fio.lowerCase() like "%${name}%".toLowerCase() }
            }

            // Добавляем пагинацию
            query = query.limit(param.limit, param.offset)

            val total = query.count()  // Получаем количество записей
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }  // Преобразуем в ответный объект

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }  // Сумма по типам

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}