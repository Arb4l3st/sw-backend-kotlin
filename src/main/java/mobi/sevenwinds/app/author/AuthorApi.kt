package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.jetbrains.annotations.NotNull
import org.joda.time.DateTime

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, CreateAuthorRequestData>(info("Добавить пользователя")) { _, body ->
            respond(AuthorService.addAuthor(body))
        }
    }
}

data class AuthorRecord(
    val fullName: String,
    val creationDateTime: DateTime
)

data class CreateAuthorRequestData(
    @NotNull val fullName: String,
)