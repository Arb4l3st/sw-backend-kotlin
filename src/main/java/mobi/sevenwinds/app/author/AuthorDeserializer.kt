package mobi.sevenwinds.app.author

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class AuthorDeserializer: JsonDeserializer<AuthorRecord>() {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): AuthorRecord {
        val root = parser.codec.readTree<JsonNode>(parser)

        return if (root.get("createdAt") != null) {
            parser.codec.treeToValue(root, AuthorResponse::class.java)
        } else if (root.get("id") != null) {
            parser.codec.treeToValue(root, AuthorIdRequest::class.java)
        } else {
            parser.codec.treeToValue(root, AuthorNameRequest::class.java)
        }
    }
}