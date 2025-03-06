package mobi.sevenwinds.app.author

object AuthorService {
    fun addAuthor(request: CreateAuthorRequest): AuthorResponse {
        val author = AuthorTable.createAuthor(request)
        return AuthorResponse(
            id = author.id,
            fullName = author.fullName,
            createdAt = author.createdAt
        )
    }
}