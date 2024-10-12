package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import kotlinx.coroutines.runBlocking
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction {
            BudgetTable.deleteAll()
            AuthorTable.deleteAll()
        }
    }

    @Test
    fun testBudgetPagination() {
        // Создаем авторов для тестов
        runBlocking {
            val authorId1 = AuthorService.addRecord("Author One")
            val authorId2 = AuthorService.addRecord("Author Two")

            // Создаем записи бюджета с указанием авторов
            addRecord(BudgetRecord(2020, 5, 10, authorId1.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 5, 5, authorId1.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 5, 20, authorId2.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 5, 30, authorId2.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 5, 40, authorId1.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2030, 1, 1, authorId1.id.value, BudgetType.Расход))

            // Запрос на получение статистики
            RestAssured.given()
                .queryParam("limit", 5)
                .queryParam("offset", 3)
                .get("/budget/year/2020/stats")
                .toResponse<BudgetYearStatsResponse>().let { response ->
                    println("${response.total} / ${response.items.size} / ${response.totalByType}")

                    Assert.assertEquals(6, response.total)
                    Assert.assertEquals(3, response.items.size)
                    Assert.assertEquals(5, response.totalByType[BudgetType.Приход.name])
                }
        }
    }

    @Test
    fun testStatsSortOrder() {
        // Создаем авторов для тестов
        runBlocking {
            val authorId = AuthorService.addRecord("Author Sort Test")

            // Добавляем записи бюджета
            addRecord(BudgetRecord(2020, 5, 100, authorId.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 1, 5, authorId.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 5, 50, authorId.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 1, 30, authorId.id.value, BudgetType.Приход))
            addRecord(BudgetRecord(2020, 5, 400, authorId.id.value, BudgetType.Приход))

            // Запрос на получение статистики с сортировкой
            RestAssured.given()
                .get("/budget/year/2020/stats?limit=100&offset=0&sort=month&order=asc")
                .toResponse<BudgetYearStatsResponse>().let { response ->
                    println(response.items)

                    Assert.assertEquals(30, response.items[0].amount)
                    Assert.assertEquals(5, response.items[1].amount)
                    Assert.assertEquals(400, response.items[2].amount)
                    Assert.assertEquals(100, response.items[3].amount)
                    Assert.assertEquals(50, response.items[4].amount)
                }
        }
    }

    @Test
    fun testInvalidMonthValues() {
        runBlocking {
            val authorId = AuthorService.addRecord("Author Sort Test")

            RestAssured.given()
                .jsonBody(BudgetRecord(2020, -5, 5, authorId.id.value, BudgetType.Приход))
                .post("/budget/add")
                .then().statusCode(400)

            RestAssured.given()
                .jsonBody(BudgetRecord(2020, 15, 5, authorId.id.value, BudgetType.Приход))
                .post("/budget/add")
                .then().statusCode(400)
        }
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }
}

