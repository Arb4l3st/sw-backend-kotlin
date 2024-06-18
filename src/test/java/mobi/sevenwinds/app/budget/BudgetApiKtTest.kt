package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorRequest
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.DateTimeComparator
import org.junit.jupiter.api.Assertions
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
    fun testAddBudget() {
        addBudget(BudgetRequest(2020, 5, 10, BudgetType.Приход))
    }

    @Test
    fun testAddBudgetWithAuthorId() {
        val author = addAndReturnAuthor(AuthorRequest("Пушкин Александр Сергеевич"))

        val request = BudgetRequest(2020, 5, 10, BudgetType.Приход, author.id.value)

        RestAssured.given()
            .jsonBody(request)
            .post("/budget/add")
            .then()
            .statusCode(200)
    }

    @Test
    fun testAddBudgetWithInvalidAuthorId() {
        val requestWithInvalidAuthorId = BudgetRequest(2020, 5, 10, BudgetType.Приход, 1)

        RestAssured.given()
            .jsonBody(requestWithInvalidAuthorId)
            .post("/budget/add")
            .then()
            .statusCode(400)
    }

    @Test
    fun testBudgetPagination() {
        addBudget(BudgetRequest(2020, 5, 10, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 5, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 20, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 30, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 40, BudgetType.Приход))
        addBudget(BudgetRequest(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assertions.assertEquals(5, response.total)
                Assertions.assertEquals(3, response.items.size)
                Assertions.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addBudget(BudgetRequest(2020, 5, 100, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 1, 5, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 50, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 1, 30, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assertions.assertEquals(30, response.items[0].amount)
                Assertions.assertEquals(5, response.items[1].amount)
                Assertions.assertEquals(400, response.items[2].amount)
                Assertions.assertEquals(100, response.items[3].amount)
                Assertions.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetResponse(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then()
            .statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetResponse(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then()
            .statusCode(400)
    }

    @Test
    fun testBudgetStatsReturnsAuthorsData() {
        val author = addAndReturnAuthor(AuthorRequest("Пушкин Александр Сергеевич"))

        addBudget(BudgetRequest(2020, 1, 1, BudgetType.Приход, author.id.value))
        addBudget(BudgetRequest(2020, 2, 1, BudgetType.Приход))

        RestAssured.given()
            .param("limit", 100)
            .param("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                Assertions.assertEquals(author.fullName, response.items[0].authorFullName)
                Assertions.assertTrue(datetimesEqual(author.createdAt, response.items[0].authorCreatedAt))
                Assertions.assertNull(response.items[1].authorFullName)
                Assertions.assertNull(response.items[1].authorCreatedAt)
            }
    }

    @Test
    fun testBudgetStatsFiltersByAuthorFullName() {
        val author = addAndReturnAuthor(AuthorRequest("Пушкин Александр Сергеевич"))

        addBudget(BudgetRequest(2020, 1, 1, BudgetType.Приход, author.id.value))
        addBudget(BudgetRequest(2020, 2, 1, BudgetType.Расход, author.id.value))
        addBudget(BudgetRequest(2020, 4, 1, BudgetType.Приход))
        addBudget(BudgetRequest(2020, 5, 1, BudgetType.Расход))

        RestAssured.given()
            .param("limit", 100)
            .param("offset", 0)
            .param("authorFullName", author.fullName)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                Assertions.assertEquals(2, response.total)
                Assertions.assertEquals(2, response.items.size)
                Assertions.assertEquals(author.fullName, response.items[0].authorFullName)
                Assertions.assertTrue(datetimesEqual(author.createdAt, response.items[0].authorCreatedAt))
                Assertions.assertEquals(author.fullName, response.items[1].authorFullName)
                Assertions.assertTrue(datetimesEqual(author.createdAt, response.items[1].authorCreatedAt))
            }
    }

    @Test
    fun testAddAuthor() {
        addAuthor(AuthorRequest("Пушкин Александр Сергеевич"))
    }

    private fun addBudget(request: BudgetRequest) {
        RestAssured.given()
            .jsonBody(request)
            .post("/budget/add")
            .then()
            .statusCode(200)
            .extract()
            .toResponse<BudgetResponse>().let { response ->
                Assertions.assertEquals(request.year, response.year)
                Assertions.assertEquals(request.month, response.month)
                Assertions.assertEquals(request.amount, response.amount)
                Assertions.assertEquals(request.type, response.type)
            }
    }

    private fun addAuthor(request: AuthorRequest) {
        RestAssured.given()
            .jsonBody(request)
            .post("/author/add")
            .then()
            .statusCode(200)
            .extract()
            .toResponse<AuthorResponse>().let { response ->
                Assertions.assertEquals(request.fullName, response.fullName)
                Assertions.assertNotNull(response.createdAt)
            }
    }

    private fun addAndReturnAuthor(request: AuthorRequest): AuthorEntity {
        addAuthor(request)

        return transaction {
            addLogger(StdOutSqlLogger)

            AuthorEntity
                .find { AuthorTable.fullName eq request.fullName }
                .orderBy(AuthorTable.createdAt to SortOrder.DESC)
                .limit(1)
                .first()
        }
    }

    private fun datetimesEqual(lhs: DateTime, rhs: DateTime?) = DateTimeComparator.getInstance().compare(lhs, rhs) == 0
}