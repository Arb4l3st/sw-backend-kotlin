package mobi.sevenwinds.app.budget

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.annotations.type.number.integer.max.Max
import com.papsign.ktor.openapigen.annotations.type.number.integer.min.Min
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import mobi.sevenwinds.app.author.AuthorApi
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorService
import org.joda.time.DateTime

fun NormalOpenAPIRoute.budget() {
    route("/budget") {
        route("/add").post<Unit, BudgetRecord, BudgetRecord>(info("Добавить запись")) { param, body ->
            respond(BudgetService.addRecord(body))
        }

        route("/author/add").post<Unit, AuthorApi.AuthorRecord, AuthorApi.AuthorRecord>(info("Добавить нового автора")) { param, body ->
            respond(AuthorService.addAuthor(body.fio))
        }

        route("/year/{year}/stats") {
            get<BudgetYearParam, BudgetYearStatsResponse>(info("Получить статистику за год")) { param ->
                val authorFioFilter = param.authorFioFilter
                respond(BudgetService.getYearStats(param, authorFioFilter))
            }
        }
    }
}

fun BudgetEntity.withAuthor(): BudgetRecordWithAuthor {
    val author = this.authorId?.let { AuthorEntity[it] }
    return BudgetRecordWithAuthor(
        year = this.year,
        month = this.month,
        amount = this.amount,
        type = this.type,
        authorFio = author?.fio,
        authorCreatedDate = author?.createdDate
    )
}

data class BudgetRecord(
    @Min(1900) val year: Int,
    @Min(1) @Max(12) val month: Int,
    @Min(1) val amount: Int,
    val type: BudgetType,
    val authorId: Int? = null
)

data class BudgetYearParam(
    @PathParam("Год") val year: Int,
    @QueryParam("Лимит пагинации") val limit: Int,
    @QueryParam("Смещение пагинации") val offset: Int,
    @QueryParam("Фильтр по ФИО автора") val authorFioFilter: String? = null
)

class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecord>
)

data class BudgetRecordWithAuthor(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType,
    val authorFio: String?,
    val authorCreatedDate: DateTime?
)
enum class BudgetType {
    Приход, Расход
}