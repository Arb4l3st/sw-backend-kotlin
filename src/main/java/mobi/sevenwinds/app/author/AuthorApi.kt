package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, AuthorRecord>(info("Добавить автора")) { _, body ->
            respond(AuthorService.addRecord(body))
        }
    }
}

data class AuthorRecord(
    val familyName: String,
    val givenName: String,
    val patronymic: String,
    val dateOfCreate: DateTime = DateTime.now()
)