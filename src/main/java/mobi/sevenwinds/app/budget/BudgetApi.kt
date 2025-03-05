package mobi.sevenwinds.app.budget

import com.fasterxml.jackson.annotation.JsonInclude
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
import org.joda.time.DateTime
import java.util.*

fun NormalOpenAPIRoute.budget() {
    route("/budget") {
        route("/add").post<Unit, BudgetResponse, BudgetRequest>(info("Добавить запись")) { _, body ->
            respond(BudgetService.addRecord(body))
        }

        route("/year/{year}/stats") {
            get<BudgetYearParam, BudgetYearStatsResponse>(info("Получить статистику за год")) { param ->
                respond(BudgetService.getYearStats(param))
            }
        }
    }
}

data class BudgetRequest(
    @Min(1900) val year: Int,
    @Min(1) @Max(12) val month: Int,
    @Min(1) val amount: Int,
    val type: BudgetType,
    val authorId: UUID? = null
)

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class BudgetResponse(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType,
    val authorFullName: String?,
    val creationDate: DateTime?
)

fun BudgetRequest.toResponse(authorFullName: String?, creationDate: DateTime?): BudgetResponse =
    BudgetResponse(
        year = year,
        month = month,
        amount = amount,
        type = type,
        authorFullName = authorFullName,
        creationDate = creationDate,
    )

data class BudgetYearParam(
    @PathParam(description = "Год")
    val year: Int,
    @QueryParam(description = "Лимит пагинации")
    val limit: Int,
    @QueryParam(description = "Смещение пагинации")
    val offset: Int,
    @QueryParam(description = "Фильтра по ФИО автора")
    val filter: String?,
)

class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetResponse>
)

enum class BudgetType {
    Приход, Расход, Комиссия
}