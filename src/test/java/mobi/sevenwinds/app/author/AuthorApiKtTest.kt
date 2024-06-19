package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest: ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testAuthorCreation() {
        val response = addAuthor(AuthorNameRequest("Дудин Яков Игоревич"))

        Assert.assertEquals("Дудин Яков Игоревич", response.name)
    }

    private fun addAuthor(record: AuthorNameRequest) =
        RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorResponse>()
}