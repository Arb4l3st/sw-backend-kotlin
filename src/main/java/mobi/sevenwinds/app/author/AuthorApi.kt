package mobi.sevenwinds.app.author

import org.joda.time.DateTime

class AuthorApi {

    data class AuthorRecord(
        val id: Int,
        val fio: String,
        val createdDate: DateTime
    )
}