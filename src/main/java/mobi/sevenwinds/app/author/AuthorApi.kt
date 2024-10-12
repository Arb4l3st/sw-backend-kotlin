package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.Length
import com.papsign.ktor.openapigen.annotations.type.string.trim.Trim
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, CreateAuthorRequest>(info("Добавить автора")) { param, body ->
            respond(AuthorService.addRecord(body))
        }
    }
}

data class CreateAuthorRequest(
    @Length(min = 1, max = 255) @Trim val fio: String
)

data class AuthorRecord(
    val id: Int,
    val fio: String,
    val createdAt: String
)