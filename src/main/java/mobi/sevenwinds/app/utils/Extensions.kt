package mobi.sevenwinds.app.utils

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Query

fun <T : Query, E : Entity<*>, X : EntityClass<*, E>> T.firstEntityOrNull(
    rowWrapper: X,
): E? {
    return limit(1).firstOrNull()?.let { rowWrapper.wrapRow(it) }
}