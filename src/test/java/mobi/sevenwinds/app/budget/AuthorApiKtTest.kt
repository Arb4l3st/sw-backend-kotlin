package mobi.sevenwinds.app.budget

import io.ktor.http.*
import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorRequest
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.author.toResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun `test add valid records returns correct response`() {
        listOf("Иванов Иван", "Петров Петр Петрович", "О Д Б", "А Я")
            .map(::AuthorRequest)
            .forEach(::addRecord)
    }

    @Test
    fun `test add invalid records not matching pattern returns bad request`() {
        listOf("иванов иван иванович", "ИИванов Иван", "English Firstname Lastname", "И1фы Ифыв Ифыв")
            .map(::AuthorRequest)
            .forEach(::addInvalidRecord)
    }

    companion object {
        fun addRecord(request: AuthorRequest): AuthorResponse = RestAssured.given()
            .jsonBody(request)
            .post("/author/create")
            .toResponse<AuthorResponse>().also { actual ->
                val expected = request.toResponse(actual.id, actual.creationDate)
                assertEquals(expected, actual)
            }

        fun addInvalidRecord(request: AuthorRequest): String = RestAssured.given()
            .jsonBody(request)
            .post("/author/create")
            .let { response ->
                val prettyMessage = response.asPrettyString()
                val expectedMessage = "ФИО должно быть в формате Фамилия Имя Отчество(если есть)."
                assertEquals(expectedMessage, prettyMessage)
                assertEquals(HttpStatusCode.BadRequest.value, response.statusCode)
                expectedMessage
            }
    }
}