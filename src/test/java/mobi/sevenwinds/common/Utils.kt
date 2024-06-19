package mobi.sevenwinds.common

import io.restassured.http.ContentType
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import mobi.sevenwinds.app.author.AuthorIdRequest
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.app.budget.BudgetRecord

fun RequestSpecification.auth(token: String): RequestSpecification = this
    .header("Authorization", "Bearer $token")

fun <T> RequestSpecification.jsonBody(body: T): RequestSpecification = this
    .body(body)
    .contentType(ContentType.JSON)

inline fun <reified T> ResponseBodyExtractionOptions.toResponse(): T {
    return this.`as`(T::class.java)
}

fun RequestSpecification.When(): RequestSpecification {
    return this.`when`()
}

fun BudgetRecord<AuthorIdRequest>.equal(response: BudgetRecord<AuthorResponse>): Boolean {
    return this.year == response.year &&
            this.amount == response.amount &&
            this.type == response.type &&
            this.month == response.month &&
            this.author?.id == response.author?.id
}