package mobi.sevenwinds.app.author

import com.papsign.ktor.openapigen.annotations.type.string.length.Length
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorRecord, AddAuthorRecordData>(info("Добавить пользователя")) {
           _, createAuthorRecordData -> respond(AuthorService.addAuthor(createAuthorRecordData))
        }
    }
}

data class AuthorRecord(
    val fullName: String,
    val creationDateTime: String
)

data class AddAuthorRecordData(
    @Length(
        min = 1,
        max = 255,
        errorMessage = "Длинна ФИО должна быть от 1 до 255."
    )
    val fullName: String,
)