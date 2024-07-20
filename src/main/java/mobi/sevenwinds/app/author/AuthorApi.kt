package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.Length
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime

fun NormalOpenAPIRoute.author() {
    route("/authors") {
        post<Unit, AuthorResponse, AuthorRequest>(info("Добавить запись")) { _, body ->
            respond(AuthorService.addRecord(body))
        }
    }
}

data class AuthorRequest(
    @Length(min = 0, max = 255) val fullName: String
)

data class AuthorResponse(
    val id: Int,
    val fullName: String,
    val createdAt: String
)