package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testAuthorAddRecord() {
        val before = DateTime.now()

        val record = AuthorRecord("Семён Семёнович Горбунков")
        println(record)

        addRecord(record).let {
            println(it)
            Assert.assertEquals(it.fio, record.fio)
            Assert.assertTrue(DateTime.parse(it.created).isAfter(before))
        }
    }

    private fun addRecord(record: AuthorRecord): AuthorResponse {
        return RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorResponse>()
    }
}