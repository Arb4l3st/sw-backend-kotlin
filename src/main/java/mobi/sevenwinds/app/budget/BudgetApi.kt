package mobi.sevenwinds.app.budget

import com.fasterxml.jackson.annotation.JsonInclude
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.annotations.type.number.integer.max.Max
import com.papsign.ktor.openapigen.annotations.type.number.integer.min.Min
import com.papsign.ktor.openapigen.annotations.type.string.length.Length
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.budget() {
    route("/budget") {
        route("/add").post<Unit, BudgetRecordData, AddBudgetRecordData>(info("Добавить запись")) { _, addBudgetRecordData ->
            respond(BudgetService.addRecord(addBudgetRecordData))
        }

        route("/year/{year}/stats") {
            get<BudgetYearParams, BudgetYearStatsData>(info("Получить статистику за год")) { budgetYearParams ->
                respond(BudgetService.getYearStats(budgetYearParams))
            }
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BudgetRecordData(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType,
    val authorName: String?,
    val authorCreationDate: String?
)

data class AddBudgetRecordData(
    @Min(DataConstraints.MIN_YEAR, "Год должен быть не раньше 1900.")
    val year: Int,

    @Min(1, "Месяц не может быть меньше 1.")
    @Max(12, "Месяц не может быть больше 12.")
    val month: Int,

    @Min(1, "Количество не может быть меньше 1.")
    val amount: Int,

    val type: BudgetType,
    val authorId: Int? = null
)

data class BudgetYearParams(
    @PathParam("Год")
    @Min(DataConstraints.MIN_YEAR)
    val year: Int,

    @QueryParam("Лимит пагинации")
    @Max(DataConstraints.MAX_LIMIT)
    @Min(DataConstraints.MIN_LIMIT)
    val limit: Int,

    @QueryParam("Смещение пагинации")
    @Max(DataConstraints.MAX_OFFSET)
    @Min(DataConstraints.MIN_OFFSET)
    val offset: Int,

    @QueryParam("Фильтр по ФИО автора")
    @Length(max = 255, min = 1)
    val authorName: String? = null,
)

class BudgetYearStatsData(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecordData>,
)

enum class BudgetType {
    Приход, Расход
}

interface DataConstraints {
    companion object {
        const val MIN_YEAR = 1900L
        const val MAX_LIMIT = 1000L
        const val MIN_LIMIT = 1L
        const val MAX_OFFSET = 10000L
        const val MIN_OFFSET = 0L
    }
}