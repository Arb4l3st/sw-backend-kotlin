package mobi.sevenwinds.app.author
import com.papsign.ktor.openapigen.annotations.type.string.length.MinLength
import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route


fun NormalOpenAPIRoute.author() {
     route("/author") {
         route("/add").post<Unit, AuthorRecord, CreateAuthorDto>(info("Добавить автора")) { _, body ->
             respond(AuthorService.addAuthorRecord(body))
         }
     }
 }

data class AuthorRecord(
    val id: Int,
    @MinLength(1) val name: String,
    val timeStamp: String
)

data class CreateAuthorDto(
    val name: String
)
