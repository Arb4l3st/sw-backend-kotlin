package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AddAuthorRecordData
import mobi.sevenwinds.app.author.AuthorRecord
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorAouTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    private fun addUserRecordAndTestCreationResult(addAuthorRecordData: AddAuthorRecordData) {
        val currentDate = DateTime().toLocalDate()
        val millis500 = 500

        RestAssured.given()
            .jsonBody(addAuthorRecordData)
            .post("/author/add")
            .toResponse<AuthorRecord>().let { response ->
                Assert.assertEquals(addAuthorRecordData.fullName, response.fullName)

                val creationDate = DateTime(response.creationDateTime).toLocalDate()

                Assert.assertEquals(currentDate, creationDate)
            }
    }

    @Test
    fun testAuthorCreationDate() {
        addUserRecordAndTestCreationResult(AddAuthorRecordData("Ivan John Petrov"))
        addUserRecordAndTestCreationResult(AddAuthorRecordData("Nikolay Daniel Kozlov"))
        addUserRecordAndTestCreationResult(AddAuthorRecordData("Massimo Joseph Magrini"))
    }
}