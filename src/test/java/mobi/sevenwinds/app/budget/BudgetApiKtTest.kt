package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import io.restassured.common.mapper.TypeRef
import io.restassured.http.ContentType
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.restassured.parsing.Parser
import mobi.sevenwinds.app.author.CreateAuthorRequest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BudgetApiKtTest : ServerTest() {

    @BeforeAll
    fun setup() {
        RestAssured.defaultParser = Parser.JSON
    }


    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testAuthorPagination() {
        addAuthor(CreateAuthorRequest("ruslan"))
        addAuthor(CreateAuthorRequest("ruslan1"))
        addAuthor(CreateAuthorRequest("ruslan2"))
        addAuthor(CreateAuthorRequest("ruslan3"))
        addAuthor(CreateAuthorRequest("ruslan4"))
        addAuthor(CreateAuthorRequest("ruslan5"))

        val authors: List<AuthorResponse> = getAuthors()

        authors.forEach { println("${it.name} ${it.createdAt}") }

        Assert.assertTrue("Список авторов не должен быть пустым", authors.isNotEmpty())
    }

    @Test
    fun testBudgetPagination() {

        addRecord(BudgetRecordRequest(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 5)
            .queryParam("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecordRequest(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRecordRequest(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .queryParam("limit", 5)
            .queryParam("offset", 0)
            .queryParam("sortBy", "month")
            .queryParam("order", "ASC")
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

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
            .jsonBody(BudgetRecordRequest(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecordRequest(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addRecord(record: BudgetRecordRequest) {
        val response: BudgetRecordResponse = RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecordResponse>()

        Assert.assertEquals(record.year, response.year)
        Assert.assertEquals(record.month, response.month)
        Assert.assertEquals(record.amount, response.amount)
        Assert.assertEquals(record.type, response.type)
        Assert.assertNull(response.author)
    }

    private fun addAuthor(record: CreateAuthorRequest) {
        RestAssured.given()
            .jsonBody(record)
            .post("/author")
            .then()
            .log().all()
            .statusCode(200)
            .body("name", equalTo(record.name))
            .body("createdAt", notNullValue())
    }

    private fun getAuthors(): List<AuthorResponse> {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .`when`()
            .get("/author")
            .then()
            .statusCode(200)
            .extract()
            .`as`(object : TypeRef<List<AuthorResponse>>() {})
    }
}