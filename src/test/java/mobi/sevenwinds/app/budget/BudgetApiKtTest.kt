package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorRequest
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.app.author.AuthorTable
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
        addRecord(BudgetRequest(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRequest(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRequest(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRequest(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assert.assertEquals(30, response.items[0].amount)
                Assert.assertEquals(5, response.items[1].amount)
                Assert.assertEquals(400, response.items[2].amount)
                Assert.assertEquals(100, response.items[3].amount)
                Assert.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRequest(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRequest(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    @Test
    fun testBudgetWithAuthors() {
        val alexId = addRecord(AuthorRequest("Alex"))
        val maxId = addRecord(AuthorRequest("Max"))


        addRecord(BudgetRequest(2020, 5, 10, BudgetType.Приход, alexId))
        addRecord(BudgetRequest(2020, 5, 5, BudgetType.Приход, alexId))
        addRecord(BudgetRequest(2020, 5, 20, BudgetType.Приход, maxId))
        addRecord(BudgetRequest(2020, 5, 30, BudgetType.Приход, maxId))
        addRecord(BudgetRequest(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRequest(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 100)
            .queryParam("offset", 0)
            .queryParam("authorName", "m")
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(2, response.items.size)
            }
    }


    private fun addRecord(record: BudgetRequest) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetResponse>().let { response ->
                Assert.assertEquals(record.type, response.type)
                Assert.assertEquals(record.amount, response.amount)
                Assert.assertEquals(record.year, response.year)
                Assert.assertEquals(record.month, response.month)
                Assert.assertEquals(record.authorId, response.author?.id)
            }
    }

    private fun addRecord(record: AuthorRequest): Int =
        RestAssured.given()
            .jsonBody(record)
            .post("/authors")
            .toResponse<AuthorResponse>().let { response ->
                Assert.assertEquals(record.fullName, response.fullName)
                response.id
            }
}