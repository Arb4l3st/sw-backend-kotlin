package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorResponse, AuthorCreateRequest>(info("Добавить автора")) { _, body ->
            respond(AuthorService.createAuthor(body))
        }
    }
}

data class AuthorCreateRequest(
    val name: String
)

data class AuthorResponse(
    val id: Int,
    val name: String,
    val createdAt: String
)