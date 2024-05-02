package mobi.sevenwinds.app.budget

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, AuthorRecord>(info("Добавить автора")) { param, body ->
            respond(
                AuthorService.addRecord(
                    AuthorRecord(
                        body.lastName,
                        body.firstName,
                        body.fatherName,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                    )
                )
            )
        }
    }
}

data class AuthorRecord(
    val lastName: String,
    val firstName: String,
    val fatherName: String,
    val dateOfCreate: String?
)