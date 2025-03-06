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
        val record = AuthorRecord("Иванов Иван Иванович")

        addRecord(record).let {
            Assert.assertEquals(it.name, record.name)
            Assert.assertTrue(it.createdDate.isAfter(DateTime.now()))
        }
    }

    private fun addRecord(record: AuthorRecord): AuthorResponse {
        return RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorResponse>()
    }
}