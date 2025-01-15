package mobi.sevenwinds.app.author

import com.fasterxml.jackson.annotation.JsonFormat
import com.papsign.ktor.openapigen.annotations.type.string.pattern.RegularExpression
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRs, AuthorRq>(info("Добавить запись")) { _, body ->
            respond(AuthorService.addRecord(body))
        }
    }
}


data class AuthorRq(
    @RegularExpression("^[А-Яа-яЁёA-Za-z- ]+$")
    val firstName: String,

    @RegularExpression("^[А-Яа-яЁёA-Za-z- ]+$")
    val lastName: String,

    val middleName: String? = null,
)


data class AuthorRs(
    val fullName: String,
    val createdAt: String
)
