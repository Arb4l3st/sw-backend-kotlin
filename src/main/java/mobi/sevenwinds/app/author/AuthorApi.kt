package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.pattern.RegularExpression
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import org.joda.time.DateTime
import java.util.*

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/create").post<Unit, AuthorResponse, AuthorRequest>(info("Создать автора")) { _, body ->
            respond(AuthorService.createAuthor(body.fullName))
        }
    }
}

data class AuthorRequest(
    @RegularExpression(
        pattern = "^[А-Я][а-я]* [А-Я][а-я]*( [А-Я][а-я]*)?$",
        errorMessage = "ФИО должно быть в формате Фамилия Имя Отчество(если есть)."
    )
    val fullName: String
)

data class AuthorResponse(
    val id: UUID,
    val creationDate: DateTime,
    val fullName: String
)

fun AuthorRequest.toResponse(id: UUID, creationDate: DateTime): AuthorResponse =
    AuthorResponse(
        id = id,
        creationDate = creationDate,
        fullName = fullName
    )
