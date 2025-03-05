package mobi.sevenwinds.app.budget

import io.ktor.http.*
import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorRequest
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
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

                assertEquals(5, response.total)
                assertEquals(3, response.items.size)
                assertEquals(105, response.totalByType[BudgetType.Приход.name])
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

                assertEquals(30, response.items[0].amount)
                assertEquals(5, response.items[1].amount)
                assertEquals(400, response.items[2].amount)
                assertEquals(100, response.items[3].amount)
                assertEquals(50, response.items[4].amount)
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
    fun `test add records with valid authors identifiers and nulls are available`() {
        val author = AuthorApiKtTest.addRecord(AuthorRequest("Иванов Иван Иванович"))

        addRecord(BudgetRequest(2020, 5, 10, BudgetType.Приход, authorId = null))
        addRecord(
            request = BudgetRequest(2020, 5, 5, BudgetType.Приход, authorId = author.id),
            authorName = author.fullName,
            creationDate = author.creationDate
        )
    }

    @Test
    fun `test add records with invalid authors returns bad request with the actual error message`() {
        listOf(
            BudgetRequest(2020, 5, 10, BudgetType.Приход, UUID.randomUUID()),
            BudgetRequest(2020, 5, 5, BudgetType.Расход, UUID.randomUUID()),
            BudgetRequest(2020, 5, 5, BudgetType.Комиссия, UUID.randomUUID())
        ).forEach(::addInvalidRecord)
    }

    private companion object {

        fun addRecord(
            request: BudgetRequest,
            authorName: String? = null,
            creationDate: DateTime? = null
        ): BudgetResponse = RestAssured.given()
            .jsonBody(request)
            .post("/budget/add")
            .also { response ->
                val jsonMap = response.body().path<Map<String, String>>("")

                val expectedAuthorInfoBeIncluded = authorName != null && creationDate != null
                val actualAreIncluded = BudgetResponse::authorFullName.name in jsonMap &&
                        BudgetResponse::creationDate.name in jsonMap

                assertEquals(expectedAuthorInfoBeIncluded, actualAreIncluded)
            }.toResponse<BudgetResponse>().also { actual ->
                val expected = request.toResponse(authorName, creationDate)

                assertEquals(expected, actual)
            }

        fun addInvalidRecord(record: BudgetRequest): String = RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .let { response ->
                val prettyMessage = response.asPrettyString()
                val expectedMessage = "Can't find author with the actual id ${record.authorId}"
                assertEquals(expectedMessage, prettyMessage)
                assertEquals(HttpStatusCode.BadRequest.value, response.statusCode)
                expectedMessage
            }
    }
}