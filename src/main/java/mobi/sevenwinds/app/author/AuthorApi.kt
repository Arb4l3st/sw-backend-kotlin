package mobi.sevenwinds.app.author

import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route

fun NormalOpenAPIRoute.author() {
    route("/author") {
        route("/add").post<Unit, AuthorResponse, AuthorNameRequest>(info("Добавить автора")) { param, body ->
            respond(AuthorService.addAuthor(body))
        }
    }
}

@JsonDeserialize(using = AuthorDeserializer::class)
interface AuthorRecord

@JsonDeserialize(using = JsonDeserializer.None::class)
data class AuthorResponse(
    val id: Int,
    @MinLength(1)
    val name: String,
    val createdAt: String
): AuthorRecord

@JsonDeserialize(using = JsonDeserializer.None::class)
data class AuthorNameRequest(
    @MinLength(1)
    val name: String
): AuthorRecord

@JsonDeserialize(using = JsonDeserializer.None::class)
data class AuthorIdRequest(
    val id: Int
): AuthorRecord