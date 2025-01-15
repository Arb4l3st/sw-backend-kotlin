package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testApiAddSuccess() {
        RestAssured.given()
            .jsonBody(AuthorRq(
                firstName = "Дмитрий",
                lastName = "Мамин-Сибиряк",
                middleName = "Наркисович"
            ))
            .post("/author/add")
            .then().statusCode(200)
    }

    @Test
    fun testCheckDatabase() {
        val countBefore = transaction {
            AuthorTable.selectAll().count()
        }

        addRecord(AuthorRq(
            firstName = "Александр",
            lastName = "Чижов"
        ))

        val rows = transaction {
            AuthorEntity
                .wrapRows(AuthorTable.selectAll())
                .map { it.toResponse() }
        }

        Assert.assertEquals(0, countBefore)
        Assert.assertEquals(1, rows.count())
        rows.forEach { it -> Assert.assertEquals("Чижов Александр", it.fullName) }
    }

    @Test
    fun testEmptyLastName() {
        RestAssured.given()
            .jsonBody(AuthorRq(
                firstName = "Александр",
                lastName = ""
            ))
            .post("/author/add")
            .then().statusCode(400)
    }

    private fun addRecord(rq: AuthorRq) {
        RestAssured.given()
            .jsonBody(rq)
            .post("/author/add")
            .toResponse<AuthorRs>()
    }
}
