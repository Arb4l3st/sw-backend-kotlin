package mobi.sevenwinds.app.author


import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.response.respond



fun NormalOpenAPIRoute.author() {
    route("/author") {
        post<Unit, AuthorResponse, CreateAuthorRequest>(info("Добавить нового автора")) { param, body ->
            val response = AuthorService.addAuthor(body)
            respond(response)
        }

        get<Unit, List<AuthorResponse>> {
            val authors = AuthorService.getAllAuthors() // Создай метод, чтобы получить всех авторов
            respond(authors)
        }
    }
}



data class CreateAuthorRequest(
    val name: String
)

data class AuthorRecord(
    val name : String
)