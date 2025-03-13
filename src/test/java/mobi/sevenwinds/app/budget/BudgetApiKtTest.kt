package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
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
        addRecord(AddBudgetRecordData(2020, 5, 10, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 5, 5, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 5, 20, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 5, 30, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 5, 40, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2030, 1, 1, BudgetType.Расход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsData>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")

                Assert.assertEquals(5, response.total)
                Assert.assertEquals(3, response.items.size)
                Assert.assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(AddBudgetRecordData(2020, 5, 100, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 1, 5, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 5, 50, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 1, 30, BudgetType.Приход))
        addRecord(AddBudgetRecordData(2020, 5, 400, BudgetType.Приход))

        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsData>().let { response ->
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
            .jsonBody(AddBudgetRecordData(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(AddBudgetRecordData(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }

    @Test
    fun testAuthorSpecification() {
        val testUserFullName = transaction {
            AuthorEntity.new { fullName = "Samuel Jakob Jefferson" }
        }

        RestAssured.given()
            .jsonBody(AddBudgetRecordData(2020, 5, 10, BudgetType.Приход, testUserFullName.id.value))
            .post("/budget/add")
            .toResponse<BudgetRecordData>().let { response ->
                Assert.assertEquals(response.authorName, testUserFullName.fullName)
                Assert.assertEquals(response.authorCreationDate, testUserFullName.creationDateTime.toString())
            }
    }

    @Test
    fun testInvalidAuthorId() {
        val nonExistentId = 999999999
        RestAssured.given()
            .jsonBody(AddBudgetRecordData(2020, 5, 10, BudgetType.Приход, nonExistentId))
            .post("/budget/add")
            .then().statusCode(400)
    }

    @Test
    fun testUserFilter() {
        //given
        val ivanNames = listOf("Ivan John Ivan", "Patrick Ivan Petrov", "Samuel Ivan Jefferson")

        val ivanContainingInNameAuthors = transaction {
            return@transaction ivanNames.map { name -> AuthorEntity.new { fullName = name } }
        }

        for (ivan in ivanContainingInNameAuthors) {
            addRecord(AddBudgetRecordData(2020, 12, 1200, BudgetType.Приход, ivan.id.value))
        }

        val author4notContainingIvan = transaction {
            AuthorEntity.new { fullName = "Samuel Jakob Jefferson" }
        }

        addRecord(AddBudgetRecordData(2020, 12, 1200, BudgetType.Приход, author4notContainingIvan.id.value))

        //check
        RestAssured.given()
            .queryParam("limit", 10)
            .queryParam("authorName", "ivan")
            .queryParam("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsData>().let { response ->
                response.items.forEach {
                    Assert.assertTrue(ivanNames.contains(it.authorName))
                    Assert.assertFalse(ivanNames.contains(author4notContainingIvan.fullName))
                }
            }
    }

    private fun addRecord(record: AddBudgetRecordData) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecordData>().let { response ->
                Assert.assertEquals(record.type, response.type)
                Assert.assertEquals(record.year, response.year)
                Assert.assertEquals(record.month, response.month)
                Assert.assertEquals(record.amount, response.amount)
            }
    }
}